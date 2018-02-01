package cz.it4i.fiji.haas;

import java.util.Arrays;

import cz.it4i.fiji.haas.JobManager.JobSynchronizableFile;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;

public class HaaSOutputHolderImpl implements HaaSOutputHolder {
	private StringBuilder result = new StringBuilder();
	private HaaSOutputSource source;
	private SynchronizableFileType type;
	public HaaSOutputHolderImpl(HaaSOutputSource source, SynchronizableFileType typeForHold) {
		this.source = source;
		this.type = typeForHold;
	}
	
	/* (non-Javadoc)
	 * @see cz.it4i.fiji.haas.HaaSOutputHolder#getActualOutput()
	 */
	@Override
	public String getActualOutput () {
		updateData();
		return result.toString();
	}

	private void updateData() {
		JobSynchronizableFile file = new JobSynchronizableFile(type, result.length());
		result.append(source.getOutput(Arrays.asList(file)).get(0));
	}
}
