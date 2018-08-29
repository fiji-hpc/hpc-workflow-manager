
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.collections.ModifiableObservableListBase;

public class SimpleObservableList<T> extends ModifiableObservableListBase<T> {

	private final List<T> innerList;
	private final Runnable addListenerCallback;
	private final Runnable removeListenerCallback;

	public SimpleObservableList(final List<T> list) {
		this(list, null, null);
	}

	public SimpleObservableList(final List<T> list, Runnable addListenerCallback,
		Runnable removeListenerCallback)
	{
		this.innerList = list;
		this.addListenerCallback = addListenerCallback;
		this.removeListenerCallback = removeListenerCallback;
	}

	@Override
	public T get(int index) {
		return innerList.get(index);
	}

	@Override
	public int size() {
		return innerList.size();
	}

	@Override
	protected void doAdd(int index, T element) {
		innerList.add(index, element);
	}

	@Override
	protected T doSet(int index, T element) {
		return innerList.set(index, element);
	}

	@Override
	protected T doRemove(int index) {
		return innerList.remove(index);
	}

	public void addListenerWithCallback(ListChangeListener<? super T> listener) {
		super.addListener(listener);
		if (addListenerCallback != null) {
			addListenerCallback.run();
		}
	}

	public void removeListenerWithCallback(
		ListChangeListener<? super T> listener)
	{
		super.removeListener(listener);
		if (removeListenerCallback != null) {
			removeListenerCallback.run();
		}
	}

}
