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
	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.HaaSOutputObservableValueRegistry.class);

	private Map<SynchronizableFileType, P_HaaSOutputObservableValue> observable = new HashMap<>();
	private List<SynchronizableFileType> types;
	private Timer timer;
	private HaaSOutputHolder holder;

	private long timeout;
	private final TimerTask task = new TimerTask() {

		@Override
		public void run() {
			update();
		}
	};

	public HaaSOutputObservableValueRegistry(HaaSOutputHolder holder,long timeout) {
		super();
		this.types = new LinkedList<SynchronizableFileType>(types);

		this.holder = holder;
		timer = new Timer();
		this.timeout = timeout;
	}

	public void start() {
		timer.schedule(task, 0);
		timer.schedule(task, timeout, timeout);
	}
	
	@Override
	public void close() {
		timer.cancel();
	}
	
	public ObservableValue<String> createObservable(SynchronizableFileType type) {
		ObservableValue<String> result;
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

		private void update(String value) {
			String oldValue = this.value;
			this.value = value;
			if (value != null && oldValue == null || value == null && oldValue != null
					|| value != null && !value.equals(oldValue)) {
				fireValueChangedEvent();
			}
		}

		@Override
		public String getValue() {
			return value;
		}

	}

}
