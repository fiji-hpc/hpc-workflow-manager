package cz.it4i.fiji.hpc_workflow.core;

import java.util.function.Consumer;
import java.util.function.Supplier;

import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowJobManager.DownloadingStatusProvider;

public class StillRunningDownloadSwitcher {

	private final DownloadingStatusProvider originalProvider;
	private final Consumer<DownloadingStatusProvider> setter;
	private final DownloadingStatusProvider providerReportingStillRunning = new DownloadingStatusProvider() {
		
		@Override
		public boolean needsDownload() {
			return true;
		}
		
		@Override
		public boolean isDownloaded() {
			return false;
		}
	};
	
	public StillRunningDownloadSwitcher(Supplier<DownloadingStatusProvider> getter, Consumer<DownloadingStatusProvider> setter) {
		originalProvider = getter.get(); 
		this.setter = setter;
		setter.accept(providerReportingStillRunning);
	}

	public synchronized void switchBack() {
		setter.accept(originalProvider);
	}

}
