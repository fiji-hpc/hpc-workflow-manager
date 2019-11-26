/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.hpc_workflow.paradigm_manager.heappe;

import org.scijava.plugin.Plugin;

import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.hpc_client.HPCClient;
import cz.it4i.fiji.hpc_workflow.commands.HaaSClientSettingsImpl;
import cz.it4i.fiji.hpc_workflow.paradigm_manager.WorkflowParadigmManager;

@Plugin(type = WorkflowParadigmManager.class)
public class HEAppEWorkflowParadigmManager extends
	WorkflowParadigmManager<HaaSClientSettingsImpl, HEAppEClientJobSettings>
{

	public HEAppEWorkflowParadigmManager()
	{
		super(HaaSClientSettingsImpl.class,
			castHaaSClient(HaaSClient.class),
			HEAppEClientJobSettings.class);
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends HPCClient<HEAppEClientJobSettings>>
		castHaaSClient(@SuppressWarnings("rawtypes") Class<HaaSClient> class1)
	{
		return (Class<? extends HPCClient<HEAppEClientJobSettings>>) class1;
	}

}
