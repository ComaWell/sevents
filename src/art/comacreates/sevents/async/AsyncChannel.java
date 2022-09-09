package art.comacreates.sevents.async;

import java.util.*;
import java.util.concurrent.*;

import art.comacreates.sevents.*;

public class AsyncChannel extends EventChannel {
	
	private final ListenerMap map = new ListenerMap();

	@Override
	public <T> void listenTo(Event<T> event, Listener<? super T> listener) {
		synchronized (map) {
			map.add(event, listener);
		}
	}

	@Override
	public <T> EventListenerException[] accept(Event<T> dispatcher, T value) {
		CompletionService<EventListenerException> completer = new ExecutorCompletionService<>(AsyncDispatcher.THREAD_POOL);
		Listeners<? super T> listeners = map.get(dispatcher);
		int dispatchCount = 0;
		for (Listener<? super T> listener : listeners.valued()) {
			dispatchCount++;
			completer.submit(() -> {
				try {
					listener.accept(dispatcher, value);
					return null;
				} catch (Exception e) {
					return new EventListenerException(dispatcher, value, listener, e);
				}
			});
		}
		for (Listener<Void> listener : listeners.blank()) {
			dispatchCount++;
			completer.submit(() -> {
				try {
					listener.accept(dispatcher, null);
					return null;
				} catch (Exception e) {
					return new EventListenerException(dispatcher, null, listener, e);
				}
			});
		}
		List<EventListenerException> failed = new ArrayList<>();
		//Wait for the tasks to finish
		for (int i = 0; i < dispatchCount; i++) {
			try {
				EventListenerException ex = completer.take().get();
				if (ex != null)
					failed.add(ex);
			} catch (InterruptedException e) {
				throw new EventDispatchException("CompletionService interrupted during dispatch", e);
			} catch (Exception e) {
				throw new EventDispatchException("Exception thrown during dispatch", e);
			}
		}
		return failed.toArray(EventListenerException[]::new);
	}

}
