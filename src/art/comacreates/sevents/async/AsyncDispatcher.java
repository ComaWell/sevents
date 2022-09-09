package art.comacreates.sevents.async;

import java.util.*;
import java.util.concurrent.*;

import art.comacreates.sevents.*;

public final class AsyncDispatcher extends EventDispatcher {
	
	static final ForkJoinPool THREAD_POOL = new ForkJoinPool();
	
	private final Set<EventChannel> channels = Collections.newSetFromMap(new ConcurrentHashMap<>());
	
	@Override
	public Environment environment() {
		return Environment.ASYNCHRONOUS;
	}
	
	@Override
	public EventChannel newChannel() {
		AsyncChannel channel = new AsyncChannel();
		channels.add(channel);
		return channel;
	}

	@Override
	public boolean killChannel(EventChannel channel) {
		return channels.remove(channel);
	}

	@Override
	protected <T> void dispatchAll(Event<T> event, T value) throws EventDispatchException {
		CompletionService<Void> completer = new ExecutorCompletionService<>(THREAD_POOL);
		int dispatchCount = 0;
		for (EventChannel channel : channels) {
			dispatchCount++;
			completer.submit(() -> {
				try {
					for (EventListenerException failed : channel.accept(event, value))
						failed.printStackTrace();
					return null;
				} catch (EventDispatchException e) {
					throw e;
				} catch (Exception e) {
					throw new EventDispatchException("Exception thrown while dispatching event " + event.name(), e);
				}
			});
		}
		try {
			for (int i = 0; i < dispatchCount; i++)
				completer.take().get();
		} catch (InterruptedException e) {
			throw new EventDispatchException("CompletionService interrupted during dispatch", e);
		} catch (Exception e) {
			throw new EventDispatchException("Exception thrown during dispatch", e);
		}
	}

}
