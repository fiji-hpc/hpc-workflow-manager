/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.hpc_workflow.paradigm_manager.heappe;

import java.io.Serializable;

import cz.it4i.fiji.haas_java_client.JobSettings;
import cz.it4i.fiji.hpc_workflow.core.JobWithWorkflowTypeSettings;

public interface HEAppEClientJobSettings extends JobSettings,
	JobWithWorkflowTypeSettings, Serializable
{

	@Override
	default long getTemplateId() {
		return getWorkflowType().getHaasTemplateID();
	}
}
