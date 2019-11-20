/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.hpc_workflow;

import java.util.Collection;
import java.util.Map;

import cz.it4i.fiji.hpc_client.JobState;

public interface TaskComputation {

	public static class Log {
		private final String name;
		private final String content;

		public Log(String name, String content) {
			this.name = name;
			this.content = content;
		}

		public String getName() {
			return name;
		}

		public String getContent() {
			return content;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((content == null) ? 0 : content.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

	}
	
	JobState getState();

	int getTimepoint();

	Map<String, Long> getOutFileSizes();

	Collection<String> getOutputs();

	Collection<Log> getLogs();

}
