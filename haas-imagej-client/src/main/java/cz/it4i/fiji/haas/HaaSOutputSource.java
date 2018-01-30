package cz.it4i.fiji.haas;

import java.util.List;

import cz.it4i.fiji.haas.JobManager.JobSynchronizableFile;

public interface HaaSOutputSource {
	public List<String> getOutput(List<JobSynchronizableFile> files);
}
