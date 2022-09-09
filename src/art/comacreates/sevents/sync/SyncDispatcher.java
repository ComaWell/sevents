package art.comacreates.sevents.sync;

import java.util.*;

import art.comacreates.sevents.*;

public final class SyncDispatcher extends EventDispatcher {

	private final Set<EventChannel> channels = Collections.synchronizedSet(new HashSet<>());
	
	@Override
	public Environment environment() {
		return Environment.SYNCHRONIZED;
	}
	
	@Override
	public EventChannel newChannel() {
		SyncChannel channel = new SyncChannel();
		channels.add(channel);
		return channel;
	}

	@Override
	public boolean killChannel(EventChannel channel) {
		return channels.remove(channel);
	}

	@Override
	synchronized
	protected <T> void dispatchAll(Event<T> event, T value) throws EventDispatchException {
		for (EventChannel channel : channels)
			try {
				for (EventListenerException failed : channel.accept(event, value))
					failed.printStackTrace();
			} catch (EventDispatchException e) {
				throw e;
			} catch (Exception e) {
				throw new EventDispatchException("Exception thrown while dispatching event " + event.name(), e);
			}
	}

}
