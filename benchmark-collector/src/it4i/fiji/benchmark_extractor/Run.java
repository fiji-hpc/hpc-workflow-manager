package it4i.fiji.benchmark_extractor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Run {

	public static void main(String[] args) {
		Path outputFile = Paths.get(args[0]);
		List<String> arguments = getAsSublist(args,1, args.length);
		Set<String> valuesToExport = new LinkedHashSet<>(arguments
				                                         .stream()
				                                         .map(str->str.contains(":")?str.split(":")[0]:str)
				                                         .collect(Collectors.toList()));
		Map<String,String> valuesWithTypes = arguments
				                             .stream()
				                             .filter(str->str.contains(":"))
				                             .map(str->str.split(":"))
				                             .collect(Collectors.toMap(pair->pair[0], pair->pair[1]));
		new Extractor(outputFile, valuesToExport,valuesWithTypes,System.out).doExtraction();
	}

	private static List<String> getAsSublist(String[] args, int start, int end) {
		List<String> result = new LinkedList<>();
		for(int i = start; i <end; i++ ) {
			result.add(args[i]);
		}
		return result;
	}

}
