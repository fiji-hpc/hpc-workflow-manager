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
	private long intervalForQueryInMs;
	private P_ResultCacheHolder<List<String>> outputCache;
	private P_ResultCacheHolder<Set<String>> changedFilesCache;
	private Map<SynchronizableFileType, Integer> allowedTypesIndices = new HashMap<>();
	private List<SynchronizableFileType> allowedTypes = new LinkedList<>();

	public SPIMComputationAccessorDecoratorWithTimeout(SPIMComputationAccessor decorated,
			Set<SynchronizableFileType> allowedTypes, long intervalForQueryInMs) {
		this.intervalForQueryInMs = intervalForQueryInMs;
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

	private void initAllowedTypes(Set<SynchronizableFileType> allowedTypes) {
		for (SynchronizableFileType type : allowedTypes) {
			this.allowedTypes.add(type);
			this.allowedTypesIndices.put(type, this.allowedTypes.size() - 1);
		}
	}

	private class P_ResultCacheHolder<T> {
		private Long lastQuery;
		private T value;
		private Function<T, T> producer;

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
