package cz.it4i.fiji.hpc_adapter;

import java.util.List;

import cz.it4i.fiji.hpc_client.SynchronizableFileType;

public interface HaaSOutputHolder {

	List<String> getActualOutput(List<SynchronizableFileType> content);

}