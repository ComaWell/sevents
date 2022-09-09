package art.comacreates.sevents;

public abstract class Event<T> {

    public static enum Type {
        BLANK,
        VALUED;
    }

    static final String ROOT_NAME = "Event";

    static final byte ROOT_GENERATION = 1;

    public static final Event.Blank ROOT = new Event.Blank(ROOT_NAME, ROOT_GENERATION, null);

    Event() { }
    
    public abstract Type type();

    public abstract String name();

    public abstract byte generation();

    public abstract Event<?> parent();

    public Event.Proxy<T> proxy() {
        return new Proxy<>(this);
    }

    public boolean isAncestorOf(Event<?> other) {
        if (other == null)
            throw new NullPointerException();
        byte gen = generation();
        byte otherGen = other.generation();
        if (gen >= otherGen)
            return false;
        for (byte i = otherGen; i > gen; i--)
            other = other.parent();
        return other == this;
    }

    public boolean isAncestorOrEqual(Event<?> other) {
        return other == this || isAncestorOf(other);
    }

    public boolean isDescendantOf(Event<?> other) {
		if (other == null)
			throw new NullPointerException();
		return other.isAncestorOf(this);
	}

    public boolean isDescendantOrEqual(Event<?> other) {
        return other == this || isDescendantOf(other);
    }

    public Event<?>[] lineage() {
        Event<?>[] lineage = new Event[generation()];
        lineage[0] = this;
        for (int i = 1; i < lineage.length; i++)
            lineage[i] = lineage[i - 1].parent();
        return lineage;
    }

    public static final class Blank extends Event<Void> {

        final String name;
        final byte generation;
        final Event<Void> parent;

        Blank(String name, byte generation, Event<Void> parent) {
            if (name == null)
                throw new NullPointerException();
            if (generation < 0)
                throw new InternalError("Event generation overflow");
            this.name = name;
            this.generation = generation;
            this.parent = parent == null ? this : parent;
        }

        @Override
        public Type type() {
            return Type.BLANK;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public byte generation() {
            return generation;
        }

        @Override
        public Event<Void> parent() {
            return parent;
        }

        public Blank blankChild(String name) {
            return new Blank(name, (byte) (generation + 1), this);
        }

        public <T> Valued<T> valuedChild(String name) {
            return new Valued<>(name, (byte) (generation + 1), this);
        }

        public void dispatch() {
            DISPATCHER.dispatch(this);
        }

    }

    public static final class Valued<T> extends Event<T> {

        final String name;
        final byte generation;
        final Event<?> parent;

        Valued(String name, byte generation, Event<?> parent) {
            if (name == null)
                throw new NullPointerException();
            if (generation < 0)
                throw new InternalError("Event generation overflow");
            this.name = name;
            this.generation = generation;
            this.parent = parent;
        }

        @Override
        public Type type() {
            return Type.VALUED;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public byte generation() {
            return generation;
        }

        @Override
        public Event<?> parent() {
            return parent;
        }

        public <C extends T> Valued<C> valuedChild(String name) {
            return new Valued<>(name, (byte) (generation + 1), this);
        }

        public T dispatch(T value) {
            if (value == null)
                throw new NullPointerException();
            DISPATCHER.dispatch(this, value);
            return value;
        }

    }

    public static final class Proxy<T> {

        final Event<T> proxied;

        private Proxy(Event<T> proxied) {
            this.proxied = proxied;
        }

        public Type type() {
            return proxied.type();
        }

        public String name() {
            return proxied.name();
        }

        public byte generation() {
            return proxied.generation();
        }

        Event<T> proxied() {
            return proxied;
        }

    }

    static EventDispatcher DISPATCHER = new EventDispatcher.Null();

    synchronized public static EventDispatcher init(EventDispatcher dispatcher) {
        if (dispatcher == null)
			throw new NullPointerException();
		if (isInit())
			throw new IllegalStateException("EventDispatcher already initialized");
		return DISPATCHER = dispatcher;
    }

    public static boolean isInit() {
		return !(DISPATCHER instanceof EventDispatcher.Null);
	}

    public static Environment environment() {
        return DISPATCHER.environment();
    }

}