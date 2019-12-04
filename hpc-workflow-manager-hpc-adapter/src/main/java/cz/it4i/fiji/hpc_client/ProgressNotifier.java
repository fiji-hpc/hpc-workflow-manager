package cz.it4i.fiji.hpc_client;

public interface ProgressNotifier {

	public void setTitle(String title);

	public void setCount(int count, int total);

	public void addItem(Object item);

	public void setItemCount(int count, int total);

	public void itemDone(Object item);

	public void done();
}
