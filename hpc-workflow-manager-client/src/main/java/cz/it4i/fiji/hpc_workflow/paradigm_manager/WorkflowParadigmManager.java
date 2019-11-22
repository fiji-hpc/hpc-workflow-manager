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
import java.util.function.Supplier;

import org.scijava.Context;
import org.scijava.parallel.ParadigmManager;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import cz.it4i.fiji.commons.UncaughtExceptionHandlerDecorator;
import cz.it4i.fiji.haas.JobWithDirectorySettings;
import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.HaaSClientSettings;
import cz.it4i.fiji.hpc_client.HPCClient;
import cz.it4i.fiji.hpc_workflow.WorkflowParadigm;
import cz.it4i.fiji.hpc_workflow.commands.FileLock;
import cz.it4i.fiji.hpc_workflow.core.AuthFailExceptionHandler;
import cz.it4i.fiji.hpc_workflow.core.AuthenticationExceptionHandler;
import cz.it4i.fiji.hpc_workflow.core.Configuration;
import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowJobManager;
import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowParameters;
import cz.it4i.fiji.hpc_workflow.core.NotConnectedExceptionHandler;
import cz.it4i.fiji.hpc_workflow.ui.ProgressDialogViewWindow;
import cz.it4i.parallel.paradigm_managers.ParadigmManagerWithSettings;
import cz.it4i.parallel.paradigm_managers.ui.HavingOwnerWindow;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;
import javafx.stage.Window;

@Plugin(type = ParadigmManager.class)
public class WorkflowParadigmManager extends
	ParadigmManagerWithSettings<HPCWorkflowParameters> implements
	HavingOwnerWindow<Window>
{

	private static final String ERROR_HEADER = "Error";
	private static final String LOCK_FILE_NAME = ".lock";

	@Parameter
	private Context context;
	private Window ownerWindow;

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
	public void prepareParadigm(ParallelizationParadigmProfile profile,
		ParallelizationParadigm paradigm)
	{
		HPCWorkflowJobManager typedParadigm = (HPCWorkflowJobManager) paradigm;
		WorkflowParadigmProfile typedProfile = (WorkflowParadigmProfile) profile;
		PManager pmManager = new PManager(typedProfile, ownerWindow);
		typedParadigm.prepareParadigm(typedProfile.getSettings()
			.workingDirectory(), getHPCClientSupplier(typedProfile.getSettings()),
			JobWithDirectorySettings.class, pmManager::init, pmManager::initDone,
			pmManager::dispose);
	}

	private static Supplier<HPCClient<JobWithDirectorySettings>>
		getHPCClientSupplier(HPCWorkflowParameters hpcWorkflowParametersImpl)
	{
		return () -> new HaaSClient<>(constructSettingsFromParams(
			hpcWorkflowParametersImpl));
	}

	@Override
	public Class<Window> getType() {
		return Window.class;
	}

	@Override
	public void setOwner(Window parent) {
		ownerWindow = parent;
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

	private static HaaSClientSettings constructSettingsFromParams(
		HPCWorkflowParameters params)
	{
		return new HaaSClientSettings() {

			@Override
			public String getUserName() {
				return params.username();
			}

			@Override
			public String getProjectId() {
				return Configuration.getHaasProjectID();
			}

			@Override
			public String getPhone() {
				return params.phone();
			}

			@Override
			public String getPassword() {
				return params.password();
			}

			@Override
			public String getEmail() {
				return params.email();
			}

		};
	}

	private static class PManager {

		private final WorkflowParadigmProfile profile;
		private UncaughtExceptionHandlerDecorator uehd;
		private FileLock fileLock;
		private ProgressDialogViewWindow progress;
		private Window ownerWindow;

		PManager(WorkflowParadigmProfile profile, Window aOwnerWindow) {
			this.profile = profile;
			this.ownerWindow = aOwnerWindow;
		}

		boolean init() {
			progress = new ProgressDialogViewWindow("Connecting to HPC", ownerWindow);
			fileLock = tryOpenWorkingDirectory(profile.getSettings()
				.workingDirectory());

			if (fileLock == null) {
				return false;
			}
			JavaFXRoutines.runOnFxThread(this::initExceptionHandler);
			return true;
		}

		void initDone() {
			progress.done();
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
