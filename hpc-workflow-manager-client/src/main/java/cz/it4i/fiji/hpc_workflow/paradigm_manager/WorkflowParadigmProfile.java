/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.hpc_workflow.paradigm_manager;

import cz.it4i.fiji.hpc_workflow.commands.HPCWorkflowParametersImpl;
import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowJobManager;
import cz.it4i.parallel.paradigm_managers.ParadigmProfileWithSettings;

public class WorkflowParadigmProfile extends
	ParadigmProfileWithSettings<HPCWorkflowParametersImpl>
{

	private static final long serialVersionUID = 6843116946587764808L;


	public WorkflowParadigmProfile(String profileName)
	{
		super(HPCWorkflowJobManager.class, profileName);
	}


	@Override
	protected Class<HPCWorkflowParametersImpl> getTypeOfSettings() {
		return HPCWorkflowParametersImpl.class;
	}

}
