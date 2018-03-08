package cz.it4i.fiji.haas_java_client;

public class DummyProgressNotifier implements ProgressNotifier {

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

	@Override
	public void setTitle(String title) {
	}
}
