package art.comacreates.sevents;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public abstract class EventDispatcher {

    public abstract Environment environment();

    public abstract EventChannel newChannel();

    public abstract boolean killChannel(EventChannel channel);

    protected abstract <T> void dispatchAll(Event<T> event, T value);

    public void dispatch(Event<Void> event) {
        dispatchAll(event, null);
    }

    public <T> void dispatch(Event<T> event, T value) {
        if (value == null)
            throw new NullPointerException();
        dispatchAll(event, value);
    }

    static final class Null extends EventDispatcher {

        @Override
        public Environment environment() {
            throw new IllegalStateException("The dispatcher has not been initialized");
        }

        @Override
        protected <T> void dispatchAll(Event<T> event, T value) {
            throw new IllegalStateException("The dispatcher has not been initialized"); 
        }

        @Override
        public EventChannel newChannel() {
            throw new IllegalStateException("The dispatcher has not been initialized"); 
        }

        @Override
        public boolean killChannel(EventChannel channel) {
            throw new IllegalStateException("The dispatcher has not been initialized");
        }
        
    }

    public static Builder builder() {
        return new Builder(null, null, null);
    }

    public static final class Builder {

        final Environment environment;
        final Consumer<EventListenerException> exceptionHandler;
        final Supplier<EventChannel> channelSupplier;

        private Builder(Environment environment, Consumer<EventListenerException> exceptionHandler, Supplier<EventChannel> channelSupplier) {
                this.environment = environment;
                this.exceptionHandler = exceptionHandler;
                this.channelSupplier = channelSupplier;
        }

        public Builder withEnvironment(Environment environment) {
            if (environment == null)
                throw new NullPointerException();
            return new Builder(environment, exceptionHandler, channelSupplier);
        }

        public Builder withExceptionHandler(Consumer<EventListenerException> exceptionHandler) {
            if (exceptionHandler == null)
                throw new NullPointerException();
            return new Builder(environment, exceptionHandler, channelSupplier);
        }

        public Builder withChannelSupplier(Supplier<EventChannel> channelSupplier) {
            if (channelSupplier == null)
                throw new NullPointerException();
            return new Builder(environment, exceptionHandler, channelSupplier);
        }

        public EventDispatcher build() {
            if (environment == null || exceptionHandler == null || channelSupplier == null)
                //TODO: Better state checking/error messages
                throw new IllegalStateException("Incomplete EventDispatcher builder");
            Set<EventChannel> channels;
            Function<Set<EventChannel>, Iterator<EventChannel>> iterator;
            switch (environment) {
                case SEQUENTIAL: {
                    channels = new HashSet<>();
                    iterator = Set::iterator;
                    break;
                }
                case SYNCHRONIZED: {
                    channels = Collections.synchronizedSet(new HashSet<>());
                    iterator = (s) -> Set.copyOf(s).iterator();
                    break;
                }
                case ASYNCHRONOUS:
                    channels = (new ConcurrentHashMap<EventChannel, Boolean>()).keySet();
                    iterator = Set::iterator;
                    break;
                default: throw new InternalError("Missing environment switch case for: " + environment);
            }
            return new EventDispatcher() {

                @Override
                public Environment environment() {
                    return environment;
                }

                @Override
                protected <T> void dispatchAll(Event<T> event, T value) {
                    try {
                        Iterator<EventChannel> it = iterator.apply(channels);
                        while (it.hasNext()) {
                            EventChannel channel = it.next();
                            for (EventListenerException failed : channel.accept(event, value))
                                exceptionHandler.accept(failed);
                        }
                    } catch (EventDispatchException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new EventDispatchException("Exception thrown while dispatching event " + event.name(), e);
                    }
                }

                @Override
                public EventChannel newChannel() {
                    EventChannel channel = channelSupplier.get();
                    if (!channels.add(channel))
                        throw new IllegalStateException("Failed to add EventChannel " + channel + " to channel set");
                    return channel;
                }

                @Override
                public boolean killChannel(EventChannel channel) {
                    if (channel == null)
                        throw new NullPointerException();
                    return channels.remove(channel);
                }

            };
        }

    }
    
}
