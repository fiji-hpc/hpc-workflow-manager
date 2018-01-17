package it4i.fiji.benchmark_collector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Run {
	public static void main(String[] args) throws IOException {

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(args[0]), "snakejob.*.*.sh.e*")) {
			Pattern p = Pattern.compile("snakejob[\\.]([^\\.]+)[\\.][0-9]+[\\.]sh[\\.]e([0-9]*)");
			Map<String, List<String>> taskName2Jobsid = new HashMap<>();
			for (Path file : stream) {
				String fileName = file.getFileName().toString();
				Matcher m = p.matcher(fileName);
				m.matches();
				String taskName = m.group(1);
				String jobId = m.group(2);
				List<String> jobIds = taskName2Jobsid.computeIfAbsent(taskName, k -> new ArrayList<String>());
				jobIds.add(jobId);
			}
			try (BufferedWriter pw = Files.newBufferedWriter(Paths.get(args[1]))) {
				for (Entry<String, List<String>> entry : taskName2Jobsid.entrySet()) {
					pw.write("Task name: " + entry.getKey() + "\n");
					pw.write("Jobs count: " + entry.getValue().size() + "\n");
					pw.write("Jobs: " + entry.getValue().stream().collect(Collectors.joining(", ")) + "\n");
					for (String jobId : entry.getValue()) {
						Process proc = new ProcessBuilder("qstat", "-xf", jobId).start();
						try (BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
							String line;
							while (null != (line = br.readLine())) {
								pw.write(line);
								pw.newLine();
							}
						}
					}

				}
			}
		}
	}
}
