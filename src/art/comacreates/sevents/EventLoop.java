package art.comacreates.sevents;

import java.util.function.Consumer;

public abstract class EventLoop {
	
	protected final EventDispatcher dispatcher;
	
	public EventLoop(EventDispatcher dispatcher) {
		if (dispatcher == null)
			throw new NullPointerException();
		this.dispatcher = dispatcher;
	}
	
	public abstract void tick();
	
	protected abstract <T> void queue(Event<T> event, T value, Runnable callback);
	
	protected abstract <T> void queue(Event<Void> event, Runnable callback);
	
	public <T> void submit(Event<T> event, T value, Runnable callback) {
		if (value == null)
			throw new NullPointerException();
		queue(event, value, callback);
	}
	
	public <T> void submit(Event<T> event, T value, Consumer<T> callback) {
		if (value == null)
			throw new NullPointerException();
		queue(event, value, () -> callback.accept(value));
	}
	
	public <T> void submit(Event<T> event, T value) {
		if (value == null)
			throw new NullPointerException();
		queue(event, value, null);
	}
	
	public void submit(Event<Void> event, Runnable callback) {
		queue(event, callback);
	}
	
	public void submit(Event<Void> event) {
		queue(event, null);
	}

}
