package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.UpdatableObservableValue;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import net.imagej.updater.util.Progress;

public class UpdatableBenchmarkJob extends UpdatableObservableValue<BenchmarkJob>{

	public static final Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.core.UpdatableBenchmarkJob.class);
	
	private P_DownloadProgress downloadProgress = new P_DownloadProgress();
	
	public interface TransferProgress {
		
		public Long getRemainingSeconds();
		
		public boolean isDownloaded();
		
		public boolean isDonwloadind();
		
		public Float getRemainingPercents();
	}
	
	public UpdatableBenchmarkJob(BenchmarkJob wrapped, Function<BenchmarkJob, UpdateStatus> updateFunction,
			Function<BenchmarkJob, Object> stateProvider) {
		super(wrapped, updateFunction, stateProvider);
		
		wrapped.setDownloadNotifier(downloadProgress);
		wrapped.resumeTransfer();
	}
	
	public void removed() {
		getValue().setDownloadNotifier(null);
	}
	
	
	
	private class P_DownloadProgress implements Progress, TransferProgress {

		private boolean downloading;
		private boolean downloaded;
		private long start;
		private Long remainingSeconds;
		private Float remainingPercents;
		
		@Override
		public void setTitle(String title) {
		}

		@Override
		public synchronized void setCount(int count, int total) {
			
			if(total < -1) {
				downloading = false;
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
		public void addItem(Object item) {
			if (!downloading) {
				downloaded = false;
				downloading = true;
				start = System.currentTimeMillis();
			}
			fireValueChangedEvent();
		}
		
		@Override
		public void done() {
			if (downloading) {
				downloaded = true;
			}
			downloading = false;
			remainingSeconds = 0l;
			remainingPercents = 0.f;
			fireValueChangedEvent();
		}

		@Override
		public boolean isDownloaded() {
			return downloaded;
		}

		@Override
		public boolean isDonwloadind() {
			return downloading;
		}

		@Override
		public Long getRemainingSeconds() {
			return remainingSeconds;
		}

		@Override
		public Float getRemainingPercents() {
			return remainingPercents;
		}
		
		@Override
		public void setItemCount(int count, int total) {
		}

		@Override
		public void itemDone(Object item) {
		}

	}

}
