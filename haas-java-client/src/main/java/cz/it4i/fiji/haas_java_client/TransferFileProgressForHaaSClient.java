package cz.it4i.fiji.haas_java_client;

import cz.it4i.fiji.scpclient.TransferFileProgress;

class TransferFileProgressForHaaSClient implements TransferFileProgress {

	private long totalSize;
	private long totalTransfered;
	private long fileSize;
	private long fileTransfered;
	
	
	
	private ProgressNotifier notifier;
	
	
	public TransferFileProgressForHaaSClient(long totalSize, ProgressNotifier notifier) {
		super();
		this.totalSize = totalSize;
		this.notifier = notifier;
	}

	public void startNewFile(long fileSize) {
		fileTransfered = 0;
		this.fileSize = fileSize;
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
}
