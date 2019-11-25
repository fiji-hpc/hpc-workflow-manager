/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.hpc_workflow.ui;

import java.util.function.Consumer;

import org.scijava.plugin.SciJavaPlugin;


public interface JavaFXJobSettingsProvider<T> extends SciJavaPlugin {

	Class<T> getTypeOfJobSettings();

	void provideJobSettings(Consumer<T> consumer);
}
