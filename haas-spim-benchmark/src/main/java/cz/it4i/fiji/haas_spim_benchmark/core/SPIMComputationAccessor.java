package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.Collection;

import cz.it4i.fiji.haas.HaaSOutputHolder;

public interface SPIMComputationAccessor extends HaaSOutputHolder {
	default boolean fileExists(String fileName) {
		return getChangedFiles().contains(fileName); 
	}
	
	Collection<String> getChangedFiles();
}
