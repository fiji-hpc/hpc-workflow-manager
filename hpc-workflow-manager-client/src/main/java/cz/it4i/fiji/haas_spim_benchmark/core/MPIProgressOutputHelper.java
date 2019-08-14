package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.Collection;
import java.util.List;

import cz.it4i.fiji.haas.Job;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;

public class MPIProgressOutputHelper implements SPIMComputationAccessor {
	
	private final Job job;	
	
	public MPIProgressOutputHelper(final Job job) {
		this.job = job;
	}

	@Override
	public List<String> getActualOutput(List<SynchronizableFileType> content) {
		return job.getOutput(null);
	}

	@Override
	public Collection<String> getChangedFiles() {
		return job.getChangedFiles();
	}

	@Override
	public List<Long> getFileSizes(List<String> names) {
		return job.getFileSizes(names);
	}

	@Override
	public List<String> getFileContents(List<String> progressLogs) {
		return job.getFileContents(progressLogs);
	}
}
