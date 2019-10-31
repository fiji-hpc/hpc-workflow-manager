/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.hpc_workflow.paradigm_manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.scijava.Context;
import org.scijava.parallel.ParadigmManager;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import cz.it4i.fiji.commons.UncaughtExceptionHandlerDecorator;
import cz.it4i.fiji.hpc_workflow.commands.FileLock;
import cz.it4i.fiji.hpc_workflow.commands.HPCWorkflowParametersImpl;
import cz.it4i.fiji.hpc_workflow.core.AuthFailExceptionHandler;
import cz.it4i.fiji.hpc_workflow.core.AuthenticationExceptionHandler;
import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowJobManager;
import cz.it4i.fiji.hpc_workflow.core.NotConnectedExceptionHandler;
import cz.it4i.fiji.hpc_workflow.ui.LoginViewWindow;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;

@Plugin(type = ParadigmManager.class)
public class WorkflowParadigmManager implements ParadigmManager {

	private static final String ERROR_HEADER = "Error";
	private static final String LOCK_FILE_NAME = ".lock";

	@Parameter
	private Context context;

	@Override
	public Class<? extends ParallelizationParadigm> getSupportedParadigmType() {
		return HPCWorkflowJobManager.class;
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
		context.inject(loginViewWindow);
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
		HPCWorkflowJobManager typedParadigm = (HPCWorkflowJobManager) paradigm;
		WorkflowParadigmProfile typedProfile = (WorkflowParadigmProfile) profile;
		PManager pmManager = new PManager(typedProfile);
		typedParadigm.prepareParadigm(typedProfile.getParameters(), pmManager::init,
			pmManager::dispose);
	}

	public static boolean checkWorkingDirectory(Path workingDirectory) {
		try (FileLock fl = tryOpenWorkingDirectory(workingDirectory)) {
			if (fl == null) {
				return false;
			}
		}
		return true;
	}

	private static FileLock tryOpenWorkingDirectory(Path workingDirectory) {
		File workingDirectoryFile = workingDirectory.toFile();
		if (!workingDirectoryFile.exists() || !workingDirectoryFile.isDirectory()) {
			JavaFXRoutines.runOnFxThread(() -> SimpleDialog.showError(ERROR_HEADER,
				"The working directory selected does not exist!"));
			return null;
		}

		FileLock result = new FileLock(workingDirectory.resolve(LOCK_FILE_NAME));
		try {
			if (!result.tryLock()) {
				JavaFXRoutines.runOnFxThread(() -> SimpleDialog.showError(ERROR_HEADER,
					"Working directory is already used by someone else."));
				return null;
			}
		}
		catch (IOException exc) {
			JavaFXRoutines.runOnFxThread(() -> SimpleDialog.showException(
				ERROR_HEADER, "Problem encountered while attempting to read file.",
				exc));
			return null;
		}
		return result;
	}

	private static class PManager {

		private final WorkflowParadigmProfile profile;
		private UncaughtExceptionHandlerDecorator uehd;
		private FileLock fileLock;

		PManager(WorkflowParadigmProfile profile) {
			this.profile = profile;
		}

		boolean init() {
			fileLock = tryOpenWorkingDirectory(profile.getParameters()
				.workingDirectory());

			if (fileLock == null) {
				return false;
			}
			JavaFXRoutines.runOnFxThread(this::initExceptionHandler);
			return true;
		}

		void dispose() {
			JavaFXRoutines.runOnFxThread(this::disposeExceptionHandler);
			fileLock.close();
			fileLock = null;
		}

		void initExceptionHandler() {
			uehd = UncaughtExceptionHandlerDecorator.setDefaultHandler();
			uehd.registerHandler(new AuthenticationExceptionHandler());
			uehd.registerHandler(new NotConnectedExceptionHandler());
			uehd.registerHandler(new AuthFailExceptionHandler());
			uehd.activate();

		}

		void disposeExceptionHandler() {
			uehd.close();
			uehd = null;
		}
	}
}
