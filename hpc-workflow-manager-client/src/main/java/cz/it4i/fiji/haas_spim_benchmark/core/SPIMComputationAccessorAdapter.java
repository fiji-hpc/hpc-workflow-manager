
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import cz.it4i.fiji.haas_java_client.SynchronizableFileType;

public class SPIMComputationAccessorAdapter implements SPIMComputationAccessor {

	@Override
	public List<String> getActualOutput(List<SynchronizableFileType> content) {
		return mapCollect(content, c -> "");
	}

	@Override
	public List<Long> getFileSizes(List<String> names) {
		return mapCollect(names, s -> 0l);
	}

	@Override
	public List<String> getFileContents(List<String> logs) {
		return mapCollect(logs, s -> "");
	}

	@Override
	public Collection<String> getChangedFiles() {
		return Collections.emptyList();
	}

	private <U, V> List<V> mapCollect(List<U> input, Function<U, V> map) {
		return input.stream().map(map).collect(Collectors.toList());
	}
}
