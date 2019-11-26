/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.hpc_workflow.paradigm_manager.ssh;

import org.scijava.plugin.Plugin;

import cz.it4i.fiji.hpc_client.HPCClient;
import cz.it4i.fiji.hpc_workflow.commands.HaaSClientSettingsImpl;
import cz.it4i.fiji.hpc_workflow.paradigm_manager.WorkflowParadigmManager;
import cz.it4i.fiji.ssh_hpc_client.SshHPCClient;

@Plugin(type = WorkflowParadigmManager.class)
public class SshWorkflowParadigmManager extends
	WorkflowParadigmManager<HaaSClientSettingsImpl, SshClientJobSettings>
{

	public SshWorkflowParadigmManager()
	{
		super(HaaSClientSettingsImpl.class,
			castSshClient(SshHPCClient.class),
			SshClientJobSettings.class);
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends HPCClient<SshClientJobSettings>>
		castSshClient(Class<SshHPCClient> class1)
	{
		return (Class<? extends HPCClient<SshClientJobSettings>>) class1;
	}

	@Override
	public String toString() {
		return "WorkflowParadigm over ssh";
	}
}
