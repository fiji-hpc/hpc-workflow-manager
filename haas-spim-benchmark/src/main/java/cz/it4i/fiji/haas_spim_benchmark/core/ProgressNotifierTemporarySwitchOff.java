package cz.it4i.fiji.haas_spim_benchmark.core;

import cz.it4i.fiji.haas.Job;
import cz.it4i.fiji.haas_java_client.ProgressNotifier;

public class ProgressNotifierTemporarySwitchOff {

	private ProgressNotifier notifier;
	private ProgressNotifier innerProgressNotifier = new ProgressNotifier() {
		
		@Override
		public void setTitle(String title) {
			notifier.setTitle(title);
		}
		
		@Override
		public void setItemCount(int count, int total) {
			//none
		}
		
		@Override
		public void setCount(int count, int total) {
			//none
			
		}
		
		@Override
		public void itemDone(Object item) {
			notifier.itemDone(item);
		}
		
		@Override
		public void done() {
			//none
		}
		
		@Override
		public void addItem(Object item) {
			notifier.addItem(item);
		}
	};
	private final Job job;
	
	public ProgressNotifierTemporarySwitchOff(ProgressNotifier downloadNotifier, Job job) {
		this.notifier = downloadNotifier;
		this.job = job;
		if(this.notifier != null) {
			job.setDownloadNotifier(innerProgressNotifier);
		}
	}

	synchronized public void switchOn() {
		if(notifier != null) {
			this.job.setDownloadNotifier(notifier);
		}
		notifier = null;
	}

}
