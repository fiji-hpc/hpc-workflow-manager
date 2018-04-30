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
	
	private Progress downloadProgress = new P_DownloadProgress();
	
	public UpdatableBenchmarkJob(BenchmarkJob wrapped, Function<BenchmarkJob, UpdateStatus> updateFunction,
			Function<BenchmarkJob, Object> stateProvider) {
		super(wrapped, updateFunction, stateProvider);
		
		wrapped.setDownloadNotifier(downloadProgress);
	}
	
	public void removed() {
		getValue().setDownloadNotifier(null);
	}
	
	
	private class P_DownloadProgress implements Progress {

		@Override
		public void setTitle(String title) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setCount(int count, int total) {
			log.info("setCount count=" + count + ", total:" + total);
		}

		@Override
		public void addItem(Object item) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setItemCount(int count, int total) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void itemDone(Object item) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void done() {
			// TODO Auto-generated method stub
			
		}
		
	}



	

}
