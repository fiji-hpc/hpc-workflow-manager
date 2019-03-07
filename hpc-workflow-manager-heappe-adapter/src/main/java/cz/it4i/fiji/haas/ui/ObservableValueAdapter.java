package cz.it4i.fiji.haas.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ObservableValueAdapter<S, T> implements ObservableValue<T> {

	private final ObservableValue<S> adapted;

	private final Function<S, T> transformation;

	private final Map<ChangeListener<? super T>, ChangeListener<? super S>> mapOfListeners = new HashMap<>();

	public ObservableValueAdapter(ObservableValue<S> decorated, Function<S, T> map) {
		this.adapted = decorated;
		this.transformation = map;
	}

	@Override
	public void addListener(InvalidationListener listener) {
		adapted.addListener(listener);

	}

	@Override
	public void removeListener(InvalidationListener listener) {
		adapted.removeListener(listener);

	}

	@Override
	public void addListener(ChangeListener<? super T> listener) {
		ChangeListener<S> wrapped = new ChangeListener<S>() {

			@Override
			public void changed(ObservableValue<? extends S> observable, S oldValue, S newValue) {
				listener.changed(ObservableValueAdapter.this, transformation.apply(oldValue),
						transformation.apply(newValue));
			}
		};
		mapOfListeners.put(listener, wrapped);
		adapted.addListener(wrapped);

	}

	@Override
	public void removeListener(ChangeListener<? super T> listener) {
		adapted.removeListener(mapOfListeners.get(listener));
	}

	@Override
	public T getValue() {
		return transformation.apply(adapted.getValue());
	}

}
