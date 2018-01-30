package cz.it4i.fiji.haas.ui;

import java.util.function.Function;

import javafx.beans.value.ObservableValueBase;

public class UpdatableObservableValue<T> extends ObservableValueBase<T>{

	public enum UpdateStatus {
		Deleted,
		Updated,
		NotUpdated
	}
	
	private T wrapped;
	private Function<T,UpdateStatus> updateFunction;
	
	
	public UpdatableObservableValue(T wrapped, Function<T, UpdateStatus> updateFunction) {
		super();
		this.wrapped = wrapped;
		this.updateFunction = updateFunction;
		
	}

	@Override
	public T getValue() {
		
		return wrapped;
	}

	public UpdateStatus update() {
		UpdateStatus status = updateFunction.apply(wrapped);
		switch (status) {
		case Updated:
			fireValueChangedEvent();
		default:
			return status;
		}

	}
	@Override
	public int hashCode() {
		return wrapped.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		UpdatableObservableValue<T> other = (UpdatableObservableValue<T>) obj;
		if (wrapped == null) {
			if (other.wrapped != null)
				return false;
		} else if (!wrapped.equals(other.wrapped))
			return false;
		return true;
	}
	
	
}
