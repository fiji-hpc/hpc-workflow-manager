/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.hpc_workflow;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.UnaryOperator;

import org.scijava.parallel.ParallelizationParadigm;

public interface WorkflowParadigm extends ParallelizationParadigm {

	WorkflowJob createJob(UnaryOperator<Path> inputProvider,
		UnaryOperator<Path> outputProvider, int numberOfNodes, int haasTemplateId)
		throws IOException;

	Collection<WorkflowJob> getJobs();

	void checkConnection();

}
