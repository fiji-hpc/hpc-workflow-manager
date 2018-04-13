package cz.it4i.fiji.haas.data_transfer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

public class FileRepository {

	private Path workingFile;
	
	private Set<Path> files = new LinkedHashSet<>();

	public FileRepository(Path workingFile) throws IOException {
		this.workingFile = workingFile;
		loadFromFile();
	}
	
	public synchronized void storeToFile() throws IOException {
		try(BufferedWriter bw = Files.newBufferedWriter(workingFile)) {
			for(Path file: files) {
				bw.write(file.toString() + "\n");
			}
		}
	}
	

	public synchronized boolean needsDownload(Path file) {
		return files.contains(file);
	}

	public synchronized void uploaded(Path p) {
		files.add(p);
		
	}

	
	private void loadFromFile() throws IOException {
		files.clear();
		try(BufferedReader br = Files.newBufferedReader(workingFile)) {
			String line;
			while(null != (line = br.readLine())) {
				processLine(line);
			}
		}
	}



	private void processLine(String line) {
		files.add(Paths.get(line));
	}
}
