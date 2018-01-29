package cz.it4i.fiji.haas;

import java.util.Arrays;

import cz.it4i.fiji.haas.JobManager.JobSynchronizableFile;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;

public class HaaSOutputHolder {
	private StringBuilder result = new StringBuilder();
	private HaaSOutputSource source;
	private SynchronizableFileType type;
	public HaaSOutputHolder(HaaSOutputSource source, SynchronizableFileType typeForHold) {
		super();
		this.source = source;
		this.type = typeForHold;
	}
	
	public String getActualOutput () {
		updateData();
		return result.toString();
	}

	private void updateData() {
		JobSynchronizableFile file = new JobSynchronizableFile(type, result.length());
		result.append(source.getOutput(Arrays.asList(file)).get(0));
	}
}
