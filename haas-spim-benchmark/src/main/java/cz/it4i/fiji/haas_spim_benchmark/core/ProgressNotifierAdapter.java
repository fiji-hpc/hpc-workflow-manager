package cz.it4i.fiji.haas_spim_benchmark.core;

import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import net.imagej.updater.util.Progress;

class ProgressNotifierAdapter implements ProgressNotifier {
	private final Progress progress;

	public ProgressNotifierAdapter(Progress progress) {
		this.progress = progress;
	}

	@Override
	public void setTitle(String title) {
		progress.setTitle(title);
	}

	@Override
	public void setCount(int count, int total) {
		progress.setCount(count, total);
	}

	@Override
	public void addItem(Object item) {
		progress.addItem(item);
	}

	@Override
	public void setItemCount(int count, int total) {
		progress.setItemCount(count, total);
	}

	@Override
	public void itemDone(Object item) {
		progress.itemDone(item);
	}

	@Override
	public void done() {
		progress.done();
	}

}