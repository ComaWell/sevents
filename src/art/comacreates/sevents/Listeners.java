package art.comacreates.sevents;

@SuppressWarnings("unchecked")
public final class Listeners<T> implements Cloneable {

    @SuppressWarnings("rawtypes")
    private static final Listener[] EMPTY = new Listener[0];

    private final Listener<Void>[] blank;
    private final Listener<? super T>[] valued;

    Listeners(Listener<Void>[] blank, Listener<? super T>[] valued) {
        this.blank = blank;
        this.valued = valued;
    }

    public Listener<Void>[] blank() {
        return blank;
    }

    public Listener<? super T>[] valued() {
        return valued;
    }

    public Listeners<T> plusBlank(Listener<Void>[] blank) {
        if (blank == null)
            throw new NullPointerException();
        if (blank.length == 0)
            return this;
        int oldLength = this.blank.length;
        int addedLength = blank.length;
        Listener<Void>[] merged = new Listener[oldLength + addedLength];
            System.arraycopy(blank, 0, merged, 0, addedLength);
            System.arraycopy(this.blank, 0, merged, addedLength, oldLength);
        return new Listeners<>(merged, valued);
    }

    public Listeners<T> plusValued(Listener<? super T>[] valued) {
        if (valued == null)
            throw new NullPointerException();
        if (valued.length == 0)
            return this;
        int oldLength = this.valued.length;
        int addedLength = valued.length;
        Listener<T>[] merged = new Listener[oldLength + addedLength];
            System.arraycopy(valued, 0, merged, 0, addedLength);
            System.arraycopy(this.valued, 0, merged, addedLength, oldLength);
        return new Listeners<>(blank, merged);
    }

    public Listeners<T> clone() {
        return new Listeners<>(blank.clone(), valued.clone());
    }

    public static Listeners<Void> of(Listener<Void>[] listeners) {
        if (listeners == null)
            throw new NullPointerException();
        return new Listeners<>(listeners, EMPTY);
    }

    public static <T> Listeners<T> of(Listener<Void>[] blank, Listener<? super T>[] valued) {
        if (blank == null || valued == null)
            throw new NullPointerException();
        return new Listeners<>(blank, valued);
    }
    
}
