package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.concurrent.Executor;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.UpdatableObservableValue;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import net.imagej.updater.util.Progress;

public class UpdatableBenchmarkJob extends UpdatableObservableValue<BenchmarkJob>{

	public static final Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.core.UpdatableBenchmarkJob.class);
	
	private P_TransferProgress downloadProgress = new P_TransferProgress();
	private P_TransferProgress uploadProgress = new P_TransferProgress();
	private Executor executor;
	public interface TransferProgress {
		
		public Long getRemainingSeconds();
		
		public boolean isDone();
		
		public boolean isWorking();
		
		public Float getRemainingPercents();
	}
	
	public UpdatableBenchmarkJob(BenchmarkJob wrapped, Function<BenchmarkJob, UpdateStatus> updateFunction,
			Function<BenchmarkJob, Object> stateProvider, Executor executorUI) {
		super(wrapped, updateFunction, stateProvider);
		
		wrapped.setDownloadNotifier(downloadProgress);
		wrapped.setUploadNotifier(uploadProgress);
		wrapped.resumeTransfer();
		this.executor = executorUI;
	}
	
	public TransferProgress getDownloadProgress() {
		return downloadProgress;
	}
	
	public TransferProgress getUploadProgress() {
		return uploadProgress;
	}
	
	public void removed() {
		getValue().setDownloadNotifier(null);
	}
	
	@Override
	protected void fireValueChangedEvent() {
		executor.execute(() -> super.fireValueChangedEvent());
	}
	
	
	private class P_TransferProgress implements Progress, TransferProgress {

		private boolean working;
		private boolean done;
		private long start;
		private Long remainingSeconds;
		private Float remainingPercents;
		
		@Override
		public synchronized void setCount(int count, int total) {
			if(total < -1) {
				working = false;
				remainingSeconds = null;
				remainingPercents = null;
			} else {
				long delta = System.currentTimeMillis() - start;
				remainingSeconds = (long) ((double) delta / count * (total - count)) / 1000;
				remainingPercents =  (((float)total - count) / total * 100);
			}
			fireValueChangedEvent();
		}

		@Override
		public synchronized void addItem(Object item) {
			if (!working) {
				done = false;
				working = true;
				start = System.currentTimeMillis();
			}
			fireValueChangedEvent();
		}
		
		@Override
		public synchronized void done() {
			if (working) {
				done = true;
			}
			working = false;
			remainingSeconds = 0l;
			remainingPercents = 0.f;
			fireValueChangedEvent();
		}

		@Override
		public synchronized boolean isDone() {
			return done;
		}

		@Override
		public synchronized boolean isWorking() {
			return working;
		}

		@Override
		public synchronized Long getRemainingSeconds() {
			return remainingSeconds;
		}

		@Override
		public synchronized Float getRemainingPercents() {
			return remainingPercents;
		}
		
		@Override
		public void setItemCount(int count, int total) {
		}

		@Override
		public void itemDone(Object item) {
		}
		
		@Override
		public void setTitle(String title) {
		}

		
	}

}
