package art.comacreates.sevents;

@FunctionalInterface
public interface Listener<T> {

    public void accept(Event<?> dispatcher, T value);
    
}
