/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.hpc_workflow.paradigm_manager;

import org.scijava.parallel.ParallelizationParadigmProfile;

import cz.it4i.fiji.hpc_workflow.commands.HPCWorkflowParametersImpl;
import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowJobManager;

public class WorkflowParadigmProfile extends ParallelizationParadigmProfile {

	private static final long serialVersionUID = 6843116946587764808L;

	private HPCWorkflowParametersImpl parameters;

	public WorkflowParadigmProfile(String profileName)
	{
		super(HPCWorkflowJobManager.class, profileName);
	}

	HPCWorkflowParametersImpl getParameters() {
		return parameters;
	}

	void setParameters(HPCWorkflowParametersImpl parameters) {
		this.parameters = parameters;
	}

}
