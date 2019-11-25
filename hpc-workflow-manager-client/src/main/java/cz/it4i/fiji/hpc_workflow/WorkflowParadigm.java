/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.hpc_workflow;

import java.io.IOException;
import java.util.Collection;

import org.scijava.parallel.ParallelizationParadigm;

public interface WorkflowParadigm<T> extends ParallelizationParadigm {

	
	WorkflowJob createJob(T parameters)
		throws IOException;

	Collection<WorkflowJob> getJobs();

	void checkConnection();

	Class<T> getTypeOfJobSettings();

}
