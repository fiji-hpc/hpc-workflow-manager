
package cz.it4i.fiji.haas.data_transfer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentIndex<T> {
	
	public static final Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.data_transfer.PersistentIndex.class);
	
	private final Path workingFile;

	private final Set<T> indexedFiles = new LinkedHashSet<>();

	private final Function<String, T> fromStringConvertor;

	public PersistentIndex(Path workingFile,Function<String, T> fromStringConvertor) throws IOException {
		this.workingFile = workingFile;
		this.fromStringConvertor = fromStringConvertor;
		loadFromWorkingFile();
	}

	public synchronized void storeToWorkingFile() throws IOException {
		try (BufferedWriter bw = Files.newBufferedWriter(workingFile)) {
			for (T file : indexedFiles) {
				bw.write(file.toString() + "\n");
			}
		}
	}

	public synchronized boolean insert(T file) {
		return indexedFiles.add(file);
	}

	public synchronized void remove(T p) {
		indexedFiles.remove(p);
	}

	public synchronized Set<T> getIndexedItems() {
		return Collections.unmodifiableSet(indexedFiles);
	}

	public synchronized void clear() throws IOException {
		indexedFiles.clear();
		storeToWorkingFile();
	}

	public synchronized boolean contains(Path file) {
		return indexedFiles.contains(file);
	}

	private void loadFromWorkingFile() throws IOException {
		indexedFiles.clear();
		if (Files.exists(workingFile)) {
			try (BufferedReader br = Files.newBufferedReader(workingFile)) {
				String line;
				while (null != (line = br.readLine())) {
					processLine(line);
				}
			}
		}
	}

	private void processLine(String line) {
		indexedFiles.add(fromStringConvertor.apply(line));
	}

}
