package cz.it4i.fiji.haas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Streams;

import cz.it4i.fiji.haas.JobManager.JobSynchronizableFile;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;

public class HaaSOutputHolderImpl implements HaaSOutputHolder {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.HaaSOutputHolderImpl.class);

	private Map<SynchronizableFileType, StringBuilder> results = new HashMap<>();
	private HaaSOutputSource source;

	public HaaSOutputHolderImpl(HaaSOutputSource source) {
		this.source = source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cz.it4i.fiji.haas.HaaSOutputHolder#getActualOutput()
	 */
	@Override
	public List<String> getActualOutput(List<SynchronizableFileType> types) {
		updateData(types);
		return types.stream().map(type -> results.get(type).toString()).collect(Collectors.toList());
	}

	private synchronized void updateData(List<SynchronizableFileType> types) {
		List<JobSynchronizableFile> files = types.stream().map(type -> new JobSynchronizableFile(type,
				results.computeIfAbsent(type, x -> new StringBuilder()).length())).collect(Collectors.toList());
		List<String> readed = source.getOutput(files);
		Streams.zip(types.stream(), readed.stream(),
				(type, content) -> (Runnable) (() -> results.get(type).append(content))).forEach(r -> r.run());
	}
}
