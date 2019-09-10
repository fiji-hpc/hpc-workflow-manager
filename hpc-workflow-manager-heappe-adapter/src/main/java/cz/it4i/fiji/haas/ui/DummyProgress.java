package cz.it4i.fiji.haas.ui;

import cz.it4i.fiji.haas_java_client.ProgressNotifier;

public class DummyProgress implements ProgressNotifier {

	@Override
	public void setTitle(String title) {

	}

	@Override
	public void setCount(int count, int total) {

	}

	@Override
	public void addItem(Object item) {

	}

	@Override
	public void setItemCount(int count, int total) {

	}

	@Override
	public void itemDone(Object item) {

	}

	@Override
	public void done() {

	}

}
