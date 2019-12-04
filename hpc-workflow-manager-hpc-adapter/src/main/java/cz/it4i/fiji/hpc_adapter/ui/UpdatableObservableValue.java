
package cz.it4i.fiji.hpc_adapter.ui;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ObservableValueBase;

public class UpdatableObservableValue<T> extends ObservableValueBase<T> {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_adapter.ui.UpdatableObservableValue.class);

	public enum UpdateStatus {
			DELETED, UPDATED, NOT_UPDATED
	}

	private final T wrapped;

	private final Function<T, UpdateStatus> updateFunction;

	private final Function<T, Object> stateProvider;

	private Object oldState;

	public UpdatableObservableValue(T wrapped,
		Function<T, UpdateStatus> updateFunction, Function<T, Object> stateProvider)
	{
		this.wrapped = wrapped;
		this.updateFunction = updateFunction;
		this.stateProvider = stateProvider;
		oldState = stateProvider.apply(wrapped);
	}

	@Override
	public T getValue() {

		return wrapped;
	}

	public UpdateStatus update() {
		UpdateStatus status = updateFunction.apply(wrapped);
		Object state = stateProvider.apply(wrapped);
		boolean fire = true;
		switch (status) {
			case NOT_UPDATED:
				fire = false;
				if (oldState == null && state != null || oldState != null &&
					(state == null || !oldState.equals(state)))
				{
					fire = true;
				}
				//$FALL-THROUGH$
			case UPDATED:
				oldState = state;
				if (fire) {
					fireValueChangedEvent();
				}
				//$FALL-THROUGH$
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
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		@SuppressWarnings("unchecked")
		UpdatableObservableValue<T> other = (UpdatableObservableValue<T>) obj;
		if (wrapped == null) {
			if (other.wrapped != null) return false;
		}
		else if (!wrapped.equals(other.wrapped)) return false;
		return true;
	}

}
