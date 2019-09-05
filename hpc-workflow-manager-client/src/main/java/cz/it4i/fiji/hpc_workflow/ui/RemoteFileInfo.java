package cz.it4i.fiji.hpc_workflow.ui;

public interface RemoteFileInfo {
	
	/**
	 * 
	 * @return size of file or -1 in case of absence
	 */
	Long getSize();
	
	String getName();
}
