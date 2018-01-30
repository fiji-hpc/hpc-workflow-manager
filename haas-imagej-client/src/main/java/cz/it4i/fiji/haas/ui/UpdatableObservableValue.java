package cz.it4i.fiji.haas.ui;

import java.util.function.Function;

import javafx.beans.value.ObservableValueBase;

public class UpdatableObservableValue<T> extends ObservableValueBase<T>{

	private T wrapped;
	private Function<T,Boolean> updateFunction;
	private Function<T,Boolean> validateFunction;
	
	
	public UpdatableObservableValue(T wrapped, Function<T, Boolean> updateFunction, Function<T, Boolean> validateFunction) {
		super();
		this.wrapped = wrapped;
		this.updateFunction = updateFunction;
		this.validateFunction = validateFunction;
	}

	@Override
	public T getValue() {
		
		return wrapped;
	}

	public boolean update() {
		if(!validateFunction.apply(wrapped)) {
			return false;
		}
		if(updateFunction.apply(wrapped)) {
			fireValueChangedEvent();
		}
		return true;
	}
	
}
