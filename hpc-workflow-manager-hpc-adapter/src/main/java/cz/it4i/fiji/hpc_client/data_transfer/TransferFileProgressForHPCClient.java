package cz.it4i.fiji.hpc_client.data_transfer;

import cz.it4i.fiji.hpc_client.ProgressNotifier;
import cz.it4i.fiji.scpclient.TransferFileProgress;

public class TransferFileProgressForHPCClient implements TransferFileProgress {

	private final long totalSize;
	private long totalTransfered;
	private long fileSize;
	private long fileTransfered;
	
	
	
	private final ProgressNotifier notifier;
	
	
	public TransferFileProgressForHPCClient(long totalSize, ProgressNotifier notifier) {
		this.totalSize = totalSize;
		this.notifier = notifier;
	}

	public void startNewFile(long initialFileSize) {
		fileTransfered = 0;
		this.fileSize = initialFileSize;
	}
	
	@Override
	public void dataTransfered(long bytesTransfered) {
		fileTransfered += bytesTransfered;
		totalTransfered += bytesTransfered;
		int[] sizes = normalizaSizes(fileTransfered, fileSize);
		notifier.setItemCount(sizes[0], sizes[1]);
		sizes = normalizaSizes(totalTransfered, totalSize);
		notifier.setCount(sizes[0], sizes[1]);
	}

	
	public void addItem(String item) {
		notifier.addItem(item);
	}

	public void itemDone(String item) {
		notifier.itemDone(item);
	}

	private static int[] normalizaSizes(long part, long total) {
		int[] result = new int[2];
		if(total > Integer.MAX_VALUE) {
			part = part>>10;
			total = total>>10;
		}
		result[0] = (int) part;
		result[1] = (int) total;
	
		if(result[0]==0 && result[1] == 0) {
			result[0] = result[1] = 1;
		}
		return result;
	}

	public void done() {
		notifier.done();
	}
}
