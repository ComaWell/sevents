package art.comacreates.sevents.wrappers;

import java.util.concurrent.atomic.*;
import java.util.function.*;

public interface Cancellable<T> {

	T value();
	
	boolean isCancelled();
	
	void setCancelled(boolean cancel);
	
	public static <T> Cancellable<T> seq(T value, boolean cancelled) {
		return new Seq<>(value, cancelled);
	}
	
	public static <T> Cancellable<T> seq(T value) {
		return seq(value, false);
	}
	
	public static <T> Cancellable<T> atomic(T value, boolean cancelled) {
		return new Atomic<>(value, cancelled);
	}
	
	public static <T> Cancellable<T> atomic(T value) {
		return atomic(value, false);
	}
	
	public static <T> Cancellable<T> proxy(T value, BooleanSupplier supplier, Consumer<Boolean> updater) {
		if (value == null || supplier == null || updater == null)
			throw new NullPointerException();
		return new Cancellable<T>() {
			
			@Override
			public T value() {
				return value;
			}
			
			@Override
			public boolean isCancelled() {
				return supplier.getAsBoolean();
			}
			
			@Override
			public void setCancelled(boolean cancel) {
				updater.accept(cancel);
			}
			
		};
	}
	
	public static <T> Cancellable<T> proxy(T value, Function<? super T, Boolean> supplier, Consumer<Boolean> updater) {
		return proxy(value, () -> supplier.apply(value), updater);
	}
	
	static final class Seq<T> implements Cancellable<T> {
		
		private final T value;
		
		private boolean cancelled;
		
		private Seq(T value, boolean cancelled) {
			if (value == null)
				throw new NullPointerException();
			this.value = value;
			this.cancelled = cancelled;
		}
		
		@Override
		public T value() {
			return value;
		}
		
		@Override
		public boolean isCancelled() {
			return cancelled;
		}
		
		@Override
		public void setCancelled(boolean cancel) {
			cancelled = cancel;
		}
		
	}
	
	static final class Atomic<T> implements Cancellable<T> {
		
		private final T value;
		
		private final AtomicBoolean cancelled;
		
		private Atomic(T value, boolean cancelled) {
			if (value == null)
				throw new NullPointerException();
			this.value = value;
			this.cancelled = new AtomicBoolean(cancelled);
		}
		
		@Override
		public T value() {
			return value;
		}
		
		@Override
		public boolean isCancelled() {
			return cancelled.get();
		}
		
		@Override
		public void setCancelled(boolean cancel) {
			cancelled.set(cancel);
		}
		
	}
	
}
