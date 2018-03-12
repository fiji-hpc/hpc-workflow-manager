package it4i.fiji.benchmark_extractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Extractor {

	private static Logger log = Logger.getLogger(it4i.fiji.benchmark_extractor.Extractor.class.getName());

	private Set<String> valuesToExport;
	private Path inputFile;
	private OutputStream out;

	private Map<String, String> valueWithTypes;

	public Extractor(Path inputFile, Set<String> valuesToExport, Map<String, String> valuesWithTypes,
			OutputStream out) {
		this.inputFile = inputFile;
		this.valuesToExport = valuesToExport;
		this.valueWithTypes = valuesWithTypes;
		this.out = out;
	}

	public void doExtraction() {
		PrintWriter pw = new PrintWriter(out);
		try (BufferedReader br = Files.newBufferedReader(inputFile)) {
			String line;
			P_Collector collector = new EmptyP_Collector();
			P_ValueCollector valueCollector = new P_ValueCollector();
			while (null != (line = br.readLine())) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				if (line.startsWith("Task name:")) {
					write(pw, collector, valueCollector);
					collector = new P_Collector(line.split(":")[1].trim());
				} else if (line.startsWith("Job Id:")) {
					collector.addJob(line.split(":")[1].trim());
				} else {
					if (line.contains(" = ")) {
						String[] tokens = line.split(" = ");
						valueCollector.startNew(collector, tokens[0], tokens[1]);
					} else {
						valueCollector.append(line);
					}
				}
			}
			if (valueCollector != null) {
				write(pw, collector, valueCollector);
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			pw.flush();
		}
	}

	private void write(PrintWriter pw, P_Collector collector, P_ValueCollector valueCollector) {
		valueCollector.flush(collector);
		collector.writeToOuput(pw);
	}

	private class P_Collector {
		private List<String> ids = new LinkedList<>();
		private Map<String, List<String>> values4Job = new HashMap<>();

		private String taskName;

		public P_Collector(String taskName) {
			super();
			this.taskName = taskName;
		}

		public void addJob(String jobId) {
			ids.add(jobId);
		}

		public void addValue(String key, String value) {
			List<String> values = values4Job.computeIfAbsent(key, this::factoryForMap);
			values.add(value);
		}

		public void writeToOuput(PrintWriter out) {
			addNullForMissingValues();
			out.printf("Task name;%s\n", taskName);
			out.printf("jobs #;%d\n", ids.size());
			out.printf("job ids;%s\n", String.join(";", ids));
			for (String key : valuesToExport) {
				out.printf("%s;%s\n", key, String.join(";", convert(key, values4Job.get(key))));
			}
		}

		private List<String> convert(String key, List<String> list) {
			if (!valueWithTypes.containsKey(key)) {
				return list;
			}
			Function<String, String> conversion = getConversion(valueWithTypes.get(key));
			return list.stream().map(conversion).collect(Collectors.toList());
		}

		private Function<String, String> getConversion(String format) {
//			Locale l =new Locale("cs");
//			NumberFormat nf = NumberFormat.getNumberInstance(l);
			switch(format) {
			case "kb":
				//return str->nf.format(Double.parseDouble(str.replace("kb", ""))/1024.);
				return str -> "" + Double.parseDouble(str.replace("kb", ""))/1024.;
			case "tm":
				
				return str->  {
					String []tokens = str.split(":");
					return Duration.ofHours(Integer.parseInt(tokens[0]))
							       .plusMinutes(Integer.parseInt(tokens[1]))
							       .plusSeconds(Integer.parseInt(tokens[2])).getSeconds() + "";
				};
			}
			
			return str->str;

		}

		private void addNullForMissingValues() {
			for (String key : valuesToExport) {
				List<String> values = values4Job.computeIfAbsent(key, this::factoryForMap);
				if (values.size() < ids.size()) {
					values.add(null);
				}
			}

		}

		private List<String> factoryForMap(String key) {
			return new LinkedList<>();
		}
	}

	private class EmptyP_Collector extends P_Collector {

		public EmptyP_Collector() {
			super(null);
		}

		@Override
		public void addValue(String key, String value) {
		}

		@Override
		public void writeToOuput(PrintWriter out) {
		}

	}

	private class P_ValueCollector {
		private String key;
		private StringBuilder value = new StringBuilder();

		public void flush(P_Collector collector) {
			if (key != null) {
				collector.addValue(key, value.toString());
				key = null;
				value.setLength(0);
			}
		}

		public void append(String line) {
			if (key != null) {
				this.value.append(line);
			}
		}

		public void startNew(P_Collector collector, String key, String value) {
			flush(collector);
			this.key = key;
			this.value.append(value);
		}
	}
}
