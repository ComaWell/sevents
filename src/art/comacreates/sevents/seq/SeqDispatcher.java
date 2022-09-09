package art.comacreates.sevents.seq;

import java.util.*;

import art.comacreates.sevents.*;

public final class SeqDispatcher extends EventDispatcher {

	private final Set<EventChannel> channels = new HashSet<>();
	
	@Override
	public Environment environment() {
		return Environment.SEQUENTIAL;
	}
	
	@Override
	public EventChannel newChannel() {
		SeqChannel channel = new SeqChannel();
		channels.add(channel);
		return channel;
	}

	@Override
	public boolean killChannel(EventChannel channel) {
		return channels.remove(channel);
	}

	@Override
	protected <T> void dispatchAll(Event<T> event, T value) {
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
