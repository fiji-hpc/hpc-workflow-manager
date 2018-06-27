package cz.it4i.fiji.haas_spim_benchmark.core;

import cz.it4i.fiji.haas.Job;
import cz.it4i.fiji.haas.ui.DummyProgress;
import cz.it4i.fiji.haas_java_client.ProgressNotifier;

public class ProgressNotifierTemporarySwitchOff {

	private final ProgressNotifier notifier;
	private final Job job;
	
	public ProgressNotifierTemporarySwitchOff(ProgressNotifier downloadNotifier, Job job) {
		this.notifier = downloadNotifier;
		this.job = job;
		if(this.notifier != null) {
			job.setDownloadNotifier(new ProgressNotifierAdapter(new DummyProgress()));
		}
	}

	public void switchOn() {
		if(notifier != null) {
			this.job.setDownloadNotifier(notifier);
		}
	}

}
