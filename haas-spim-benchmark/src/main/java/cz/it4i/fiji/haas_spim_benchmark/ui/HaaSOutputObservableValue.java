package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.io.Closeable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

import javafx.beans.value.ObservableValueBase;

public class HaaSOutputObservableValue extends ObservableValueBase<String> implements Closeable {

	private Supplier<String> supplier;
	private String value;
	private Timer timer;

	public HaaSOutputObservableValue(Supplier<String> supplier, long timeout) {
		super();
		this.supplier = supplier;
		value = supplier.get();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				update();
			}
		}, timeout, timeout);
	}

	@Override
	public String getValue() {
		return value;
	}

	private void update() {
		String oldValue = value;
		value = supplier.get();
		if (value != null && oldValue == null || value == null && oldValue != null
				|| value != null && !value.equals(oldValue)) {
			fireValueChangedEvent();
		}
	}

	@Override
	public void close() {
		timer.cancel();
	}

}
