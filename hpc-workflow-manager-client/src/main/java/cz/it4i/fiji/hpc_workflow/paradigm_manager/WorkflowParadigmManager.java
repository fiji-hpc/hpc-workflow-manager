/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.hpc_workflow.paradigm_manager;

import org.scijava.parallel.ParadigmManager;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.plugin.Plugin;

import cz.it4i.fiji.hpc_workflow.WorkflowParadigm;
import cz.it4i.fiji.hpc_workflow.commands.HPCWorkflowParametersImpl;
import cz.it4i.fiji.hpc_workflow.ui.LoginViewWindow;

@Plugin(type = ParadigmManager.class)
public class WorkflowParadigmManager implements ParadigmManager {

	@Override
	public Class<? extends ParallelizationParadigm> getSupportedParadigmType() {
		return WorkflowParadigm.class;
	}

	@Override
	public boolean isProfileSupported(ParallelizationParadigmProfile profile) {
		return profile instanceof WorkflowParadigmProfile;
	}

	@Override
	public ParallelizationParadigmProfile createProfile(String name) {
		return new WorkflowParadigmProfile(name);
	}

	@Override
	public boolean editProfile(ParallelizationParadigmProfile profile) {
		LoginViewWindow loginViewWindow = new LoginViewWindow();
		WorkflowParadigmProfile typedProfile = (WorkflowParadigmProfile) profile;
		loginViewWindow.openWindow(typedProfile.getParameters());
		HPCWorkflowParametersImpl newParameters = loginViewWindow.getParameters();
		if (newParameters != null) {
			typedProfile.setParameters(newParameters);
			return true;
		}
		return false;
	}

	@Override
	public void prepareParadigm(ParallelizationParadigmProfile profile,
		ParallelizationParadigm paradigm)
	{
		throw new UnsupportedOperationException();
	}

}
