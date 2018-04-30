package cz.it4i.fiji.haas.ui;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import cz.it4i.fiji.haas.ui.UpdatableObservableValue.UpdateStatus;
import javafx.beans.value.ObservableValue;

public class ObservableValueRegistry<T> {

	private Function<T,UpdateStatus> updateFunction;
	private Consumer<T> removeConsumer;
	private Function<T, Object> stateProvider;
	
	
	public ObservableValueRegistry(Function<T, UpdateStatus> updateFunction,Function<T,Object> stateProvider,
			Consumer<T> removeConsumer) {
		this.updateFunction = updateFunction;
		this.stateProvider = stateProvider;
		this.removeConsumer = t-> {
			removeConsumer.accept(t);
			remove(t);
		};
		
	}

	private Map<T,UpdatableObservableValue<T>> map = new LinkedHashMap<>(); 
	
	public  ObservableValue<T> addIfAbsent(T value) {
		UpdatableObservableValue<T> uov = map.computeIfAbsent(value, v-> constructObservableValue(v, updateFunction, stateProvider));
		return uov;
	}

	protected UpdatableObservableValue<T> constructObservableValue(T v, Function<T, UpdateStatus> updateFunction, Function<T, Object> stateProvider) {
		return new UpdatableObservableValue<T>(v, updateFunction, stateProvider);
	}
	
	public UpdatableObservableValue<T> get(T value) {
		return map.get(value);
	}
	
	public Collection<ObservableValue<T>> getAllItems() {
		return map.values().stream().map(val->(ObservableValue<T>)val).collect(Collectors.toList());
	}
	
	protected ObservableValue<T> remove(T value) {
		return map.remove(value);
	}
	
	public void update() {
		for (UpdatableObservableValue<T> value : new LinkedList<>(map.values())) {
			if(value.update() == UpdateStatus.Deleted) {
				removeConsumer.accept(value.getValue());
			}
		}
	}
	
	protected void setUpdateFunction(Function<T, UpdateStatus> updateFunction) {
		this.updateFunction = updateFunction;
	}
}
