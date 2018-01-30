package cz.it4i.fiji.haas_spim_benchmark.core;

import cz.it4i.fiji.haas.HaaSOutputHolder;

public interface SPIMComputationAccessor extends HaaSOutputHolder {
	boolean fileExists(String fileName);
}
