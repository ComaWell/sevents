package art.comacreates.sevents;

public class EventListenerException extends RuntimeException {

	private static final long serialVersionUID = 787694083970915543L;
	
	private final Event<?> dispatcher;
	private final Object value;
	private final Listener<?> listener;
	
	public EventListenerException(Event<?> dispatcher, Object value, Listener<?> listener, Exception cause) {
		super(cause);
		if (dispatcher == null || (dispatcher.type() == Event.Type.VALUED && value == null) || listener == null)
			throw new NullPointerException();
		this.dispatcher = dispatcher;
		this.value = value;
		this.listener = listener;
	}
	
	public Event<?> dispatcher() {
		return dispatcher;
	}

	public Object value() {
		return value;
	}

	public Listener<?> listener() {
		return listener;
	}

}
