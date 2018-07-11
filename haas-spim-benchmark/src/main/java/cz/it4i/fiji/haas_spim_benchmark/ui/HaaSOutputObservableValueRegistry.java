package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.io.Closeable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Streams;

import cz.it4i.fiji.haas.HaaSOutputHolder;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;

public class HaaSOutputObservableValueRegistry implements Closeable {

	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.HaaSOutputObservableValueRegistry.class);

	private final Map<SynchronizableFileType, P_HaaSOutputObservableValue> observable = new HashMap<>();	

	private final List<SynchronizableFileType> types = new LinkedList<>();
	
	private final Timer timer;
	
	private final HaaSOutputHolder holder;
	
	private final long timeout;


	public HaaSOutputObservableValueRegistry(HaaSOutputHolder holder, long timeout) {
		this.holder = holder;
		timer = new Timer();
		this.timeout = timeout;
	}

	public void start() {
		timer.schedule(createTask(), 0);
		timer.schedule(createTask(), timeout, timeout);
	}

	private TimerTask createTask() {
		return new TimerTask() {

			@Override
			public void run() {
				update();
			}
		};
	}

	@Override
	public void close() {
		timer.cancel();
	}

	public ObservableValue<String> createObservable(SynchronizableFileType type) {
		ObservableValue<String> result;
		types.add(type);
		observable.put(type, (P_HaaSOutputObservableValue) (result = new P_HaaSOutputObservableValue()));
		return result;
	}

	private void update() {
		List<String> values = holder.getActualOutput(types);
		Streams.zip(types.stream(), values.stream(),
				(type, value) -> (Runnable) (() -> observable.get(type).update(value))).forEach(r -> r.run());
	}

	private class P_HaaSOutputObservableValue extends ObservableValueBase<String> {

		String value;

		private synchronized void update(String newValue) {
			String oldValue = this.value;
			this.value = newValue;
			if (newValue != null && oldValue == null || newValue == null && oldValue != null
					|| newValue != null && !newValue.equals(oldValue)) {
				fireValueChangedEvent();
			}
		}

		@Override
		public String getValue() {
			return value;
		}

	}

}
