package cz.it4i.fiji.haas.data_transfer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentIndex<T> {
	
	public static final Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.data_transfer.PersistentIndex.class);
	
	private Path workingFile;

	private Set<T> files = new LinkedHashSet<>();

	private Function<String, T> fromString;

	public PersistentIndex(Path workingFile,Function<String, T> fromString) throws IOException {
		this.workingFile = workingFile;
		this.fromString = fromString;
		loadFromFile();
	}

	public synchronized void storeToFile() throws IOException {
		try (BufferedWriter bw = Files.newBufferedWriter(workingFile)) {
			for (T file : files) {
				bw.write(file.toString() + "\n");
			}
		}
	}

	public synchronized boolean insert(T file) {
		return files.add(file);
	}

	public synchronized void remove(T p) {
		files.remove(p);
	}

	public synchronized void fillQueue(Queue<T> toUpload) {
		toUpload.addAll(files);
	}
	
	public synchronized void clear() throws IOException {
		files.clear();
		storeToFile();
	}

	public synchronized boolean contains(Path file) {
		return files.contains(file);
	}

	private void loadFromFile() throws IOException {
		files.clear();
		if(Files.exists(workingFile)) {
			try (BufferedReader br = Files.newBufferedReader(workingFile)) {
				String line;
				while (null != (line = br.readLine())) {
					processLine(line);
				}
			}
		}
	}

	private void processLine(String line) {
		files.add(fromString.apply(line));
	}

	
}
