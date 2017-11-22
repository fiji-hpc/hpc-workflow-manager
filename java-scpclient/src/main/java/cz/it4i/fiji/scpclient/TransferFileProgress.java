package cz.it4i.fiji.scpclient;

public interface TransferFileProgress {
	long getMinimalDeltaForNotification();
	void dataTransfered(long bytesTransfered);
}
