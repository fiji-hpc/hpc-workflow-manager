/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/

package cz.it4i.fiji.hpc_workflow.core;

public enum JobType {
		SPIM_WORKFLOW(4), MACRO(8), SCRIPT(16);

	private final int haasTemplateID;

	private JobType(int jobType) {
		this.haasTemplateID = jobType;
	}

	public int getHaasTemplateID() {
		return this.haasTemplateID;
	}

	public static JobType forLong(long id) {
		for (JobType jobType : values()) {
			if (jobType.haasTemplateID == id) {
				return jobType;
			}
		}
		throw new IllegalArgumentException("Invalid JobType id: " + id);
	}

}
