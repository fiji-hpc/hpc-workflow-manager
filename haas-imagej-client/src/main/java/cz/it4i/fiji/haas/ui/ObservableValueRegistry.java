package cz.it4i.fiji.haas.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.value.ObservableValue;

public class ObservableValueRegistry<T> {

	private Function<T, Boolean> updateFunction;
	private Function<T, Boolean> validateFunction;
	private Consumer<T> removeConsumer;
	
	
	public ObservableValueRegistry(Function<T, Boolean> validateFunction, Function<T, Boolean> updateFunction,
			Consumer<T> removeConsumer) {
		super();
		this.validateFunction = validateFunction;
		this.updateFunction = updateFunction;
		this.removeConsumer = removeConsumer;
	}

	private Map<T,UpdatableObservableValue<T>> map = new HashMap<>(); 
	
	public  ObservableValue<T> addIfAbsent(T value) {
		UpdatableObservableValue<T> uov = map.computeIfAbsent(value, v-> new UpdatableObservableValue<T>(v, updateFunction, validateFunction));
		return uov;
	}
	
	public ObservableValue<T> remove(T value) {
		return map.get(value);
	}
	
	public void update() {
		for (UpdatableObservableValue<T> value : new LinkedList<>(map.values())) {
			if(!value.update()) {
				removeConsumer.accept(value.getValue());
			}
		}
	}
}
