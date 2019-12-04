package cz.it4i.fiji.hpc_adapter;

import java.util.List;

import cz.it4i.fiji.hpc_adapter.JobManager.JobSynchronizableFile;

public interface HaaSOutputSource {
	public List<String> getOutput(List<JobSynchronizableFile> files);
}
