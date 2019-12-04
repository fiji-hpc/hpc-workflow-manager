package cz.it4i.fiji.hpc_adapter.ui;

import cz.it4i.fiji.hpc_client.ProgressNotifier;

public class DummyProgress implements ProgressNotifier {

	@Override
	public void setTitle(String title) {
		// Empty because it is a dummy.
	}

	@Override
	public void setCount(int count, int total) {
		// Empty because it is a dummy.
	}

	@Override
	public void addItem(Object item) {
		// Empty because it is a dummy.
	}

	@Override
	public void setItemCount(int count, int total) {
		// Empty because it is a dummy.
	}

	@Override
	public void itemDone(Object item) {
		// Empty because it is a dummy.
	}

	@Override
	public void done() {
		// Empty because it is a dummy.
	}

}
