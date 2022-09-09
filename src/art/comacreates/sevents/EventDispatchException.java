package art.comacreates.sevents;

public class EventDispatchException extends RuntimeException {

	private static final long serialVersionUID = 657853991041379261L;
	
	public EventDispatchException(String message, Exception cause) {
		super(message, cause);
	}
	
	public EventDispatchException(Exception cause) {
		super(cause);
	}

}
