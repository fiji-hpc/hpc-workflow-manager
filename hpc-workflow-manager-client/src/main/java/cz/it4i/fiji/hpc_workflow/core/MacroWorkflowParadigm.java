/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/

package cz.it4i.fiji.hpc_workflow.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.UnaryOperator;

import cz.it4i.fiji.hpc_workflow.WorkflowJob;
import cz.it4i.fiji.hpc_workflow.WorkflowParadigm;

public interface MacroWorkflowParadigm extends WorkflowParadigm {

	WorkflowJob createJob(UnaryOperator<Path> inputProvider,
		UnaryOperator<Path> outputProvider, int numberOfNodes, int haasTemplateId,
		Callable<String> userScriptName) throws IOException;
}
