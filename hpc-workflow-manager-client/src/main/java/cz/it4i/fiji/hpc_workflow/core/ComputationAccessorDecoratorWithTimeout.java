package cz.it4i.fiji.hpc_workflow.core;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import cz.it4i.fiji.hpc_client.SynchronizableFileType;

public class ComputationAccessorDecoratorWithTimeout implements ComputationAccessor {
	private final long intervalForQueryInMs;
	private final  PResultCacheHolder<List<String>> outputCache;
	private final PResultCacheHolder<Set<String>> changedFilesCache;
	private final Map<SynchronizableFileType, Integer> allowedTypesIndices =
		new EnumMap<>(SynchronizableFileType.class);
	private final List<SynchronizableFileType> allowedTypes = new LinkedList<>();
	private ComputationAccessor decorated;

	public ComputationAccessorDecoratorWithTimeout(ComputationAccessor decorated,
			Set<SynchronizableFileType> allowedTypes, long intervalForQueryInMs) {
		this.intervalForQueryInMs = intervalForQueryInMs;
		this.decorated = decorated;
		initAllowedTypes(allowedTypes);
		outputCache = new PResultCacheHolder<>(x -> decorated.getActualOutput(this.allowedTypes));
		changedFilesCache = new PResultCacheHolder<>(set -> {
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

	private void initAllowedTypes(Set<SynchronizableFileType> usedAllowedTypes) {
		for (SynchronizableFileType type : usedAllowedTypes) {
			this.allowedTypes.add(type);
			this.allowedTypesIndices.put(type, this.allowedTypes.size() - 1);
		}
	}

	private class PResultCacheHolder<T> {
		private Long lastQuery;
		private T value;
		private final Function<T, T> producer;

		public PResultCacheHolder(UnaryOperator<T> producer) {
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
