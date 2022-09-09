package art.comacreates.sevents.async;

import java.util.*;
import java.util.concurrent.*;

import art.comacreates.sevents.*;

public class AsyncLoop extends EventLoop {
	
	private final Queue<Runnable> queue = new ArrayDeque<>();

	public AsyncLoop(EventDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public void tick() {
		CompletionService<Void> completer = new ExecutorCompletionService<>(AsyncDispatcher.THREAD_POOL);
		int dispatchCount = 0;
		synchronized (queue) {
			while (!queue.isEmpty()) {
				dispatchCount++;
				completer.submit(queue.remove(), null);
			}
			try {
				for (int i = 0; i < dispatchCount; i++)
					completer.take().get();
			} catch (EventDispatchException e) {
				throw e;
			} catch (InterruptedException e) {
				throw new EventDispatchException("CompletionService interrupted during dispatch", e);
			} catch (Exception e) {
				throw new EventDispatchException("Exception thrown during dispatch", e);
			}
		}
	}

	@Override
	synchronized
	protected <T> void queue(Event<T> event, T value, Runnable callback) {
		queue.add(callback == null
				? () -> dispatcher.dispatch(event, value)
				: () -> {
					dispatcher.dispatch(event, value);
					callback.run();
				});
	}

	@Override
	synchronized
	protected <T> void queue(Event<Void> event, Runnable callback) {
		queue.add(callback == null
				? () -> dispatcher.dispatch(event)
				: () -> {
					dispatcher.dispatch(event);
					callback.run();
				});
	}

}
