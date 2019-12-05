/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.hpc_workflow.core;

public enum WorkflowType {
		SPIM_WORKFLOW(4), MACRO_WORKFLOW(8);

	private final int haasTemplateID;

	private WorkflowType(int workflowType) {
		this.haasTemplateID = workflowType;
	}

	public int getHaasTemplateID() {
		return this.haasTemplateID;
	}

	public static WorkflowType forLong(long id) {
		for (WorkflowType workflows : values()) {
			if (workflows.haasTemplateID == id) {
				return workflows;
			}
		}
		throw new IllegalArgumentException("Invalid WorkflowType id: " + id);
	}

}