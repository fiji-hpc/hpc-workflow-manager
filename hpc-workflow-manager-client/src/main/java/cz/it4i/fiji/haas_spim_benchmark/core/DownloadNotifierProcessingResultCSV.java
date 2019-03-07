
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;

class DownloadNotifierProcessingResultCSV implements ProgressNotifier {

	public static final Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_spim_benchmark.core.DownloadNotifierProcessingResultCSV.class);

	private final ProgressNotifier decorated;

	private final BenchmarkJob job;

	public DownloadNotifierProcessingResultCSV(final ProgressNotifier decorated,
		final BenchmarkJob job)
	{
		this.decorated = decorated;
		this.job = job;
	}

	@Override
	public void setTitle(final String title) {
		decorated.setTitle(title);
	}

	@Override
	public void setCount(final int count, final int total) {
		decorated.setCount(count, total);
	}

	@Override
	public void addItem(final Object item) {
		decorated.addItem(item);
	}

	@Override
	public void setItemCount(final int count, final int total) {
		decorated.setItemCount(count, total);
	}

	@Override
	public void itemDone(final Object item) {
		if (item instanceof String && ((String)item).endsWith(Constants.BENCHMARK_RESULT_FILE)) {
			final Path resultFile = job.getDirectory().resolve(Constants.BENCHMARK_RESULT_FILE);
			try {
				if (resultFile != null) BenchmarkJobManager.formatResultFile(resultFile);
			} 
			catch (RuntimeException e) {
				log.warn("parsing result file failed", e);
			}
		}
		decorated.itemDone(item);
	}

	@Override
	public void done() {
		decorated.done();
	}

}
