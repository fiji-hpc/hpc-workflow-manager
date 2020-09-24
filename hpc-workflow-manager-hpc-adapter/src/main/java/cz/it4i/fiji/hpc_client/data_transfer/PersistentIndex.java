
package cz.it4i.fiji.hpc_client.data_transfer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PersistentIndex<T> {

	public static final Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_client.data_transfer.PersistentIndex.class);

	private final Path workingFile;

	private Set<T> indexedFiles = new LinkedHashSet<>();
	private Map<String, Long> lastUpdatedfiles = new HashMap<>();

	private final Function<String, T> fromStringConvertor;

	public PersistentIndex(Path workingFile,
		Function<String, T> fromStringConvertor) throws IOException
	{
		this.workingFile = workingFile;
		this.fromStringConvertor = fromStringConvertor;
		loadFromWorkingFile();
	}

	public synchronized void storeToWorkingFile() throws IOException {
		try (BufferedWriter bw = Files.newBufferedWriter(workingFile)) {
			for (T file : indexedFiles) {
				File myFile = new File(file.toString());
				bw.write(file.toString() + "," + myFile.lastModified() + "\n");
			}
		}
	}

	public synchronized boolean insert(T file) {
		File myFile = new File(file.toString());
		lastUpdatedfiles.put(file.toString(), myFile.lastModified());
		return indexedFiles.add(file);
	}

	public synchronized void remove(T p) {
		lastUpdatedfiles.remove(p.toString());
		indexedFiles.remove(p);
	}

	public synchronized Set<T> getIndexedItems() {
		return Collections.unmodifiableSet(indexedFiles);
	}

	public synchronized void clear() throws IOException {
		indexedFiles.clear();
		storeToWorkingFile();
	}

	public synchronized boolean contains(T file) {
		boolean containsItem = false;
		File myFile = new File(file.toString());
		if (indexedFiles.contains(file) && lastUpdatedfiles.containsKey(file
			.toString()) && myFile.lastModified() == lastUpdatedfiles.get(file
				.toString()))
		{
			containsItem = true;
		}
		return containsItem;
	}

	private void loadFromWorkingFile() throws IOException {
		indexedFiles.clear();
		String[] pathAndTime;
		String path = "";
		Long time = 0L;
		if (workingFile.toFile().exists()) {
			try (BufferedReader br = Files.newBufferedReader(workingFile)) {
				String line;
				while (null != (line = br.readLine())) {
					pathAndTime = line.split(",");
					if(pathAndTime.length == 2) {
						path = pathAndTime[0];
						time = Long.parseLong(pathAndTime[1]);
					} else {
						path = pathAndTime[0];
					}
					lastUpdatedfiles.put(path, time);
					processLine(pathAndTime[0]);
				}
			}
		}
	}

	private void processLine(String line) {
		indexedFiles.add(fromStringConvertor.apply(line));
	}

}
