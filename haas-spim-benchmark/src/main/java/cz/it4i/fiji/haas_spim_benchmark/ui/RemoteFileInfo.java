package cz.it4i.fiji.haas_spim_benchmark.ui;

public interface RemoteFileInfo {
	
	/**
	 * 
	 * @return size of file or -1 in case of absence
	 */
	long getSize();
	
	String getName();
}
