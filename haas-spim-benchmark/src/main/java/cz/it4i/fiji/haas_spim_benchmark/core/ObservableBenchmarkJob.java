
package cz.it4i.fiji.haas_spim_benchmark.core;

import com.google.common.collect.Streams;

import java.io.Closeable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.imagej.updater.util.Progress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.UpdatableObservableValue;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;

public class ObservableBenchmarkJob extends
	UpdatableObservableValue<BenchmarkJob> implements Closeable
{

	public static final Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_spim_benchmark.core.ObservableBenchmarkJob.class);

	private final P_TransferProgress downloadProgress = new P_TransferProgress(
		val -> getValue().setDownloaded(val), () -> getValue().isDownloaded(),
		() -> getValue().needsDownload());
	private final P_TransferProgress uploadProgress = new P_TransferProgress(
		val -> getValue().setUploaded(val), () -> getValue().isUploaded(),
		() -> getValue().needsUpload());
	private final Executor executor;

	private final P_Observable fileTransferObservable = new P_Observable();

	private final HaaSOutputObservableValueRegistry observableValueRegistry;

	public interface TransferProgress {

		public Long getRemainingMiliseconds();

		public boolean isDone();

		public boolean isWorking();

		public Float getRemainingPercents();
	}

	public ObservableBenchmarkJob(BenchmarkJob wrapped,
		Function<BenchmarkJob, UpdateStatus> updateFunction,
		Function<BenchmarkJob, Object> stateProvider, Executor executorUI)
	{
		super(wrapped, updateFunction, stateProvider);
		this.executor = executorUI;
		wrapped.setDownloadNotifier(downloadProgress);
		wrapped.setUploadNotifier(uploadProgress);
		wrapped.resumeTransfer();

		observableValueRegistry = new HaaSOutputObservableValueRegistry();
	}

	public TransferProgress getDownloadProgress() {
		return downloadProgress;
	}

	public TransferProgress getUploadProgress() {
		return uploadProgress;
	}

	public void removed() {
		getValue().setDownloadNotifier(null);
		getValue().setUploadNotifier(null);
	}

	public void startObservingFileTransfer(final Observer observer) {
		fileTransferObservable.addObserver(observer);
	}

	public void stopObservingFileTransfer(final Observer observer) {
		fileTransferObservable.deleteObserver(observer);
	}

	public ObservableValue<String> getObservableSnakemakeOutput(
		SynchronizableFileType type)
	{
		return observableValueRegistry.observableValues.get(type);
	}

	@Override
	public void close() {
		observableValueRegistry.close();
	}

	@Override
	protected void fireValueChangedEvent() {
		executor.execute(() -> {
			super.fireValueChangedEvent();
		});
	}

	private void notifyFileTransferObservers() {
		fileTransferObservable.setChanged();
		fileTransferObservable.notifyObservers();
	}

	// -- Private classes --

	private class P_Observable extends Observable {

		@Override
		public synchronized void setChanged() {
			super.setChanged();
		}
	}

	private class P_TransferProgress implements Progress, TransferProgress {

		private final Supplier<Boolean> doneStatusSupplier;
		private final Consumer<Boolean> doneStatusConsumer;
		private final Supplier<Boolean> workingSupplier;
		private Long start;
		private Long remainingMiliseconds;
		private Float remainingPercents;

		public P_TransferProgress(Consumer<Boolean> doneStatusConsumer,
			Supplier<Boolean> doneStatusSupplier, Supplier<Boolean> workingSupplier)
		{
			this.doneStatusConsumer = doneStatusConsumer;
			this.doneStatusSupplier = doneStatusSupplier;
			this.workingSupplier = workingSupplier;
		}

		@Override
		public synchronized void setCount(int count, int total) {
			if (total < -1) {
				clearProgress();
			}
			else if (start != null) {
				long delta = System.currentTimeMillis() - start;
				remainingMiliseconds = (long) ((double) delta / count * (total -
					count));
				remainingPercents = (((float) total - count) / total * 100);
			}
			fireValueChangedEvent();
		}

		@Override
		public synchronized void addItem(Object item) {
			if (start == null) {
				setDone(false);
				clearProgress();
				start = System.currentTimeMillis();
				fireValueChangedEvent();
			}
		}

		@Override
		public synchronized void done() {
			remainingMiliseconds = 0l;
			remainingPercents = 0.f;
			start = null;
			fireValueChangedEvent();
		}

		@Override
		public synchronized boolean isWorking() {
			return workingSupplier.get();
		}

		@Override
		public synchronized Long getRemainingMiliseconds() {
			return remainingMiliseconds;
		}

		@Override
		public synchronized Float getRemainingPercents() {
			return remainingPercents;
		}

		@Override
		public void setItemCount(int count, int total) {}

		@Override
		public void itemDone(final Object item) {
			notifyFileTransferObservers();
		}

		@Override
		public void setTitle(String title) {}

		@Override
		public boolean isDone() {
			return doneStatusSupplier.get();
		}

		private void clearProgress() {
			remainingMiliseconds = null;
			remainingPercents = null;
		}

		private void setDone(boolean val) {
			doneStatusConsumer.accept(val);
		}
	}

	private class HaasOutputObservableValue extends ObservableValueBase<String> {

		private String wrappedValue;

		private synchronized void update(String newValue) {
			String oldValue = this.wrappedValue;
			this.wrappedValue = newValue;
			if (newValue != null && oldValue == null || newValue == null &&
				oldValue != null || newValue != null && !newValue.equals(oldValue))
			{
				fireValueChangedEvent();
			}
		}

		@Override
		public String getValue() {
			return wrappedValue;
		}

	}

	private class HaaSOutputObservableValueRegistry implements Closeable {

		private final Map<SynchronizableFileType, HaasOutputObservableValue> observableValues =
			new HashMap<>();
		private final Timer timer;

		public HaaSOutputObservableValueRegistry() {
			this.observableValues.put(SynchronizableFileType.StandardOutputFile,
				new HaasOutputObservableValue());
			this.observableValues.put(SynchronizableFileType.StandardErrorFile,
				new HaasOutputObservableValue());
			this.timer = new Timer();
			this.timer.schedule(new TimerTask() {

				@Override
				public void run() {

					final List<SynchronizableFileType> types = new LinkedList<>(
						observableValues.keySet());

					Streams.zip(types.stream(), getValue().getComputationAccessor()
						.getActualOutput(types).stream(), (type,
							value) -> (Runnable) (() -> observableValues.get(type).update(
								value))).forEach(r -> r.run());
				}
			}, 0, Constants.HAAS_UPDATE_TIMEOUT /
				Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO);
		}

		@Override
		public synchronized void close() {
			timer.cancel();
		}
	}

}
