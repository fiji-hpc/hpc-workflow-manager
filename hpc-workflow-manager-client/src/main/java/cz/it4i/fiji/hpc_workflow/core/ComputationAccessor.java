package cz.it4i.fiji.hpc_workflow.core;

import java.util.Collection;
import java.util.List;

import cz.it4i.fiji.hpc_adapter.HaaSOutputHolder;

public interface ComputationAccessor extends HaaSOutputHolder {
	
	final String FILE_SEPARATOR_UNIX = "/";
	
	default boolean fileExists(String fileName) {
		return getChangedFiles().contains(FILE_SEPARATOR_UNIX + fileName);
	}
	Collection<String> getChangedFiles();

	List<Long> getFileSizes(List<String> names);
	
	List<String> getFileContents(List<String> logs);
}
