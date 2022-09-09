package art.comacreates.sevents;

public abstract class EventChannel {

    public abstract <T> EventListenerException[] accept(Event<T> event, T value);

    public abstract <T> void listenTo(Event<T> event, Listener<? super T> listener);

    public void listenTo(Event<?> event, Runnable listener) {
        if (listener == null)
			throw new NullPointerException();
        listenTo(event, (e, v) -> listener.run());
    }

    public final <T> void listenTo(Event.Proxy<T> proxy, Listener<T> listener) {
        if (proxy == null)
            throw new NullPointerException();
        listenTo(proxy.proxied(), listener);
    }

    public final void listenTo(Event.Proxy<?> proxy, Runnable listener) {
        if (proxy == null)
			throw new NullPointerException();
        listenTo(proxy.proxied(), listener);
    }
    
}
