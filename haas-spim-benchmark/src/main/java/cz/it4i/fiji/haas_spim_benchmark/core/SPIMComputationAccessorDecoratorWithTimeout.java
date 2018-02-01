package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class SPIMComputationAccessorDecoratorWithTimeout implements SPIMComputationAccessor {
	private long intervalForQueryInMs;

	private P_ResultCacheHolder<String> outputCache;

	private P_ResultCacheHolder<Set<String>> changedFilesCache;

	public SPIMComputationAccessorDecoratorWithTimeout(SPIMComputationAccessor decorated, long intervalForQueryInMs) {
		super();
		this.intervalForQueryInMs = intervalForQueryInMs;
		outputCache = new P_ResultCacheHolder<>(x -> decorated.getActualOutput());
		changedFilesCache = new P_ResultCacheHolder<>(set -> {
			if (set == null) {
				set = new HashSet<>();
			} else {
				set.clear();
			}
			set.addAll(decorated.getChangedFiles());
			return null;
		});
	}

	@Override
	public String getActualOutput() {

		return outputCache.getResult();
	}

	@Override
	public Collection<String> getChangedFiles() {
		return changedFilesCache.getResult();
	}

	private class P_ResultCacheHolder<T> {
		private Long lastQuery;
		private T value;
		private Function<T, T> producer;

		public P_ResultCacheHolder(Function<T, T> producer) {
			super();
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
