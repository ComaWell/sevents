package art.comacreates.sevents.sync;

import java.util.*;

import art.comacreates.sevents.*;

public class SyncLoop extends EventLoop {
	
	private final Queue<Runnable> queue = new ArrayDeque<>();

	public SyncLoop(EventDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	synchronized
	public void tick() {
		try {
			while (!queue.isEmpty())
				queue.remove().run();
		} catch (EventDispatchException e) {
			throw e;
		} catch (Exception e) {
			throw new EventDispatchException("Exception occured while ticking EventLoop", e);
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
