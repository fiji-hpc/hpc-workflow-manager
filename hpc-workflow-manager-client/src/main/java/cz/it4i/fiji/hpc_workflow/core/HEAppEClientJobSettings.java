/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.hpc_workflow.core;

import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import cz.it4i.fiji.haas_java_client.JobSettings;
import lombok.Builder;
import lombok.Getter;

@Builder
public class HEAppEClientJobSettings implements JobSettings {

	@Getter
	private final UnaryOperator<Path> inputDirectoryProvider;

	@Getter
	private final UnaryOperator<Path> outputDirectoryProvider;

	@Getter
	private final int numberOfNodes;

	@Getter
	private final long templateId;

	@Getter
	private final Supplier<String> userScriptName;

	@Getter
	private final String jobName;

	@Getter
	private final int walltimeLimit;

	@Getter
	private final long clusterNodeType;

	@Getter
	private final int numberOfCoresPerNode;
}
