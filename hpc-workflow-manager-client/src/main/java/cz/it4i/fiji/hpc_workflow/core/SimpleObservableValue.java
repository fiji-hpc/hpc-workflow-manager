
package cz.it4i.fiji.hpc_workflow.core;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValueBase;

public class SimpleObservableValue<T> extends ObservableValueBase<T> {

	private T innerValue;
	private final Runnable addListenerCallback;
	private final Runnable removeListenerCallback;

	public SimpleObservableValue(final T value) {
		this(value, null, null);
	}

	public SimpleObservableValue(final T value, Runnable addListenerCallback,
		Runnable removeListenerCallback)
	{
		this.innerValue = value;
		this.addListenerCallback = addListenerCallback;
		this.removeListenerCallback = removeListenerCallback;
	}

	@Override
	public void addListener(final ChangeListener<? super T> listener) {
		super.addListener(listener);
		if (addListenerCallback != null) {
			addListenerCallback.run();
		}
	}

	@Override
	public void removeListener(final ChangeListener<? super T> listener) {
		super.removeListener(listener);
		if (removeListenerCallback != null) {
			removeListenerCallback.run();
		}
	}

	@Override
	public T getValue() {
		return innerValue;
	}

	public synchronized void update(T newValue) {
		if (this.innerValue != null && !this.innerValue.equals(newValue) ||
			newValue != null && !newValue.equals(this.innerValue))
		{
			this.innerValue = newValue;
			fireValueChangedEvent();
		}
	}

}
