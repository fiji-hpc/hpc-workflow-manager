package cz.it4i.fiji.haas_spim_benchmark.core;

import cz.it4i.fiji.haas.Job;
import cz.it4i.fiji.haas_java_client.ProgressNotifier;

public class ProgressNotifierTemporarySwitchOff {

	private ProgressNotifier notifier;

	private final Job job;

	public ProgressNotifierTemporarySwitchOff(ProgressNotifier downloadNotifier,
		Job job)
	{
		this.notifier = downloadNotifier;
		this.job = job;
		if (this.notifier != null) {
			job.setDownloadNotifier(new PInnerProgressNotifier(this.notifier));
		}
	}

	public synchronized void switchOn() {
		if (notifier != null) {
			this.job.setDownloadNotifier(notifier);
		}
		notifier = null;
	}

	private static class PInnerProgressNotifier implements ProgressNotifier {

		private final ProgressNotifier innerNotifier;

		public PInnerProgressNotifier(ProgressNotifier innerNotifier) {
			this.innerNotifier = innerNotifier;
		}

		@Override
		public void setTitle(String title) {
			innerNotifier.setTitle(title);
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
			innerNotifier.itemDone(item);
		}
		
		@Override
		public void done() {
			//none
		}
		
		@Override
		public void addItem(Object item) {
			innerNotifier.addItem(item);
		}
	}
}
