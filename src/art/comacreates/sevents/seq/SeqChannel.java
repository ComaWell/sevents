package art.comacreates.sevents.seq;

import java.util.*;

import art.comacreates.sevents.*;

public class SeqChannel extends EventChannel {
	
	private final ListenerMap map = new ListenerMap();

	@Override
	public <T> void listenTo(Event<T> event, Listener<? super T> listener) {
		map.add(event, listener);
	}

	@Override
	public <T> EventListenerException[] accept(Event<T> dispatcher, T value) {
		Listeners<? super T> listeners = map.get(dispatcher);
		List<EventListenerException> failed = new ArrayList<>();
		for (Listener<? super T> listener : listeners.valued())
			try {
				listener.accept(dispatcher, value);
			} catch (Exception e) {
				failed.add(new EventListenerException(dispatcher, value, listener, e));
			}
		for (Listener<Void> listener : listeners.blank())
			try {
				listener.accept(dispatcher, null);
			} catch (Exception e) {
				failed.add(new EventListenerException(dispatcher, null, listener, e));
			}
		return failed.toArray(EventListenerException[]::new);
	}

}
