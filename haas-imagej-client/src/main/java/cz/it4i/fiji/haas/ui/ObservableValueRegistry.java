package cz.it4i.fiji.haas.ui;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import cz.it4i.fiji.haas.ui.UpdatableObservableValue.UpdateStatus;

public abstract class ObservableValueRegistry<K, V extends UpdatableObservableValue<K>> {

	private Function<K, UpdateStatus> updateFunction;

	private final Consumer<K> removeConsumer;
	
	private final Function<K, Object> stateProvider;
	
	private final Map<K, V> map = new LinkedHashMap<>();

	public ObservableValueRegistry(Function<K, UpdateStatus> updateFunction, Function<K, Object> stateProvider,
			Consumer<K> removeConsumer) {
		this.updateFunction = updateFunction;
		this.stateProvider = stateProvider;
		this.removeConsumer = k -> {
			removeConsumer.accept(k);
			remove(k);
		};
	}
	
	public V addIfAbsent(K key) {
		V uov = map.computeIfAbsent(key, k -> constructObservableValue(k));
		return uov;
	}
	
	public V get(K key) {
		return map.get(key);
	}

	public Collection<V> getAllItems() {
		return map.values().stream().map(val -> val).collect(Collectors.toList());
	}
	
	public void update() {
		for (V value : new LinkedList<>(map.values())) {
			if (value.update() == UpdateStatus.Deleted) {
				removeConsumer.accept(value.getValue());
			}
		}
	}
	
	abstract protected V constructObservableValue(K k);

	protected V remove(K key) {
		return map.remove(key);
	}

	protected void setUpdateFunction(Function<K, UpdateStatus> updateFunction) {
		this.updateFunction = updateFunction;
	}
	
	protected Function<K, UpdateStatus> getUpdateFunction() {
		return updateFunction;
	}
	
	protected Function<K, Object> getStateProvider() {
		return stateProvider;
	}
}
