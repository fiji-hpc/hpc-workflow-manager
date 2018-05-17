package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import cz.it4i.fiji.haas_java_client.SynchronizableFileType;

public class SPIMComputationAccessorDecoratorWithTimeout implements SPIMComputationAccessor {
	private final long intervalForQueryInMs;
	private final  P_ResultCacheHolder<List<String>> outputCache;
	private final P_ResultCacheHolder<Set<String>> changedFilesCache;
	private final Map<SynchronizableFileType, Integer> allowedTypesIndices = new HashMap<>();
	private final List<SynchronizableFileType> allowedTypes = new LinkedList<>();
	private SPIMComputationAccessor decorated;

	public SPIMComputationAccessorDecoratorWithTimeout(SPIMComputationAccessor decorated,
			Set<SynchronizableFileType> allowedTypes, long intervalForQueryInMs) {
		this.intervalForQueryInMs = intervalForQueryInMs;
		this.decorated = decorated;
		initAllowedTypes(allowedTypes);
		outputCache = new P_ResultCacheHolder<List<String>>(x -> decorated.getActualOutput(this.allowedTypes));
		changedFilesCache = new P_ResultCacheHolder<>(set -> {
			if (set == null) {
				set = new HashSet<>();
			} else {
				set.clear();
			}
			set.addAll(decorated.getChangedFiles());
			return set;
		});
	}

	@Override
	public synchronized List<String> getActualOutput(List<SynchronizableFileType> types) {
		if (!allowedTypesIndices.keySet().containsAll(types)) {
			throw new IllegalArgumentException("supported only types: " + allowedTypes + ", given" + types);
		}
		List<String> result = outputCache.getResult();
		return types.stream().map(type -> result.get(allowedTypesIndices.get(type))).collect(Collectors.toList());
	}

	@Override
	public synchronized Collection<String> getChangedFiles() {
		return changedFilesCache.getResult();
	}

	@Override
	public List<Long> getFileSizes(List<String> names) {
		return decorated.getFileSizes(names);
	}
	
	@Override
	public List<String> getFileContents(List<String> logs) {
		return decorated.getFileContents(logs);
	}

	private void initAllowedTypes(Set<SynchronizableFileType> allowedTypes) {
		for (SynchronizableFileType type : allowedTypes) {
			this.allowedTypes.add(type);
			this.allowedTypesIndices.put(type, this.allowedTypes.size() - 1);
		}
	}

	private class P_ResultCacheHolder<T> {
		private Long lastQuery;
		private T value;
		private final Function<T, T> producer;

		public P_ResultCacheHolder(Function<T, T> producer) {
			this.producer = producer;
		}

		public T getResult() {
			long time = System.currentTimeMillis();

			if (lastQuery == null || (time - lastQuery) > intervalForQueryInMs) {
				value = producer.apply(value);
				lastQuery = time;
			}
			return value;
		}
	}
}
