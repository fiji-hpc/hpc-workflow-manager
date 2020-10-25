/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/

package cz.it4i.fiji.hpc_workflow.paradigm_manager;

import java.io.File;
import java.nio.file.Path;

import org.scijava.Context;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.plugin.Parameter;

import cz.it4i.common.UncaughtExceptionHandlerDecorator;
import cz.it4i.fiji.hpc_client.HPCClient;
import cz.it4i.fiji.hpc_workflow.commands.FileLock;
import cz.it4i.fiji.hpc_workflow.core.AuthFailExceptionHandler;
import cz.it4i.fiji.hpc_workflow.core.AuthenticationExceptionHandler;
import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowJobManager;
import cz.it4i.fiji.hpc_workflow.core.JobWithJobTypeSettings;
import cz.it4i.fiji.hpc_workflow.core.NotConnectedExceptionHandler;
import cz.it4i.fiji.hpc_workflow.ui.HPCWorkflowWindow;
import cz.it4i.fiji.hpc_workflow.ui.ProgressDialogViewWindow;
import cz.it4i.parallel.paradigm_managers.ParadigmManagerWithSettings;
import cz.it4i.parallel.paradigm_managers.ParadigmProfileWithSettings;
import cz.it4i.parallel.paradigm_managers.ui.HavingOwnerWindow;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;
import javafx.stage.Window;

public class WorkflowParadigmManager<T extends SettingsWithWorkingDirectory, U extends JobWithJobTypeSettings>
	extends ParadigmManagerWithSettings<SettingsWithWorkingDirectory> implements
	HavingOwnerWindow<Window>
{

	private static final String ERROR_HEADER = "Error";
	private static final String LOCK_FILE_NAME = ".lock";

	private Class<T> typeOfSettings;

	private Class<? extends HPCClient<? super U>> typeOfClient;

	@Parameter
	private Context context;
	private Window ownerWindow;
	private Class<U> typeOfJobSettings;

	public WorkflowParadigmManager(Class<T> typeOfSettings,
		Class<? extends HPCClient<? super U>> typeOfClient,
		Class<U> typeOfJobSettings)
	{
		super();
		this.typeOfSettings = typeOfSettings;
		this.typeOfClient = typeOfClient;
		this.typeOfJobSettings = typeOfJobSettings;
	}

	@Override
	public Class<? extends ParallelizationParadigm> getSupportedParadigmType() {
		return HPCWorkflowJobManager.class;
	}

	@Override
	public boolean isProfileSupported(ParallelizationParadigmProfile profile) {
		if (getSupportedParadigmType().equals(profile.getParadigmType()) &&
			profile instanceof WorkflowParadigmProfile)
		{
			WorkflowParadigmProfile<?, ?> typedProfile =
				(WorkflowParadigmProfile<?, ?>) profile;
			return typedProfile.getTypeOfClient().equals(typeOfClient);
		}
		return false;
	}

	@Override
	public ParadigmProfileWithSettings<T> createProfile(String name) {
		return new WorkflowParadigmProfile<>(name, typeOfSettings, typeOfClient);
	}

	@Override
	public void prepareParadigm(ParallelizationParadigmProfile profile,
		ParallelizationParadigm paradigm)
	{
		@SuppressWarnings("unchecked")
		HPCWorkflowJobManager<U> typedParadigm =
			(HPCWorkflowJobManager<U>) paradigm;
		@SuppressWarnings("unchecked")
		WorkflowParadigmProfile<T, U> typedProfile =
			(WorkflowParadigmProfile<T, U>) profile;

		PManager pmManager = new PManager(typedProfile, ownerWindow);
		typedParadigm.prepareParadigm(typedProfile.getSettings()
			.getWorkingDirectory(), typedProfile::createHPCClient, typeOfJobSettings,
			pmManager::init, pmManager::initDone, pmManager::dispose);
	}

	@Override
	public Class<Window> getType() {
		return Window.class;
	}

	@Override
	public void setOwner(Window parent) {
		ownerWindow = parent;
	}

	public static boolean workingDirectoryExists(Path workingDirectory) {
		File workingDirectoryFile = workingDirectory.toFile();
		return (workingDirectoryFile.exists() && workingDirectoryFile
			.isDirectory());
	}

	private static FileLock tryOpenWorkingDirectory(Path workingDirectory) {
		if (!workingDirectoryExists(workingDirectory)) {
			JavaFXRoutines.runOnFxThread(() -> SimpleDialog.showError(ERROR_HEADER,
				"The working directory " + workingDirectory.toString() +
					" selected does not exist!"));
			return null;
		}

		FileLock result = new FileLock(workingDirectory.resolve(LOCK_FILE_NAME));
		try {
			if (!result.tryLock()) {
				JavaFXRoutines.runOnFxThread(() -> SimpleDialog.showError(ERROR_HEADER,
					"Working directory is already used by someone else."));
				HPCWorkflowWindow.windowIsOpen = false;
				result = null;
			}
		}
		catch (Exception exc) {
			JavaFXRoutines.runOnFxThread(() -> SimpleDialog.showException(
				ERROR_HEADER, "Problem encountered while attempting to read file.",
				exc));
			result = null;
		}
		return result;
	}

	private static class PManager {

		private final WorkflowParadigmProfile<?, ?> profile;
		private UncaughtExceptionHandlerDecorator uehd;
		private FileLock fileLock;
		private ProgressDialogViewWindow progress;
		private Window ownerWindow;

		PManager(WorkflowParadigmProfile<?, ?> profile, Window aOwnerWindow) {
			this.profile = profile;
			this.ownerWindow = aOwnerWindow;
		}

		boolean init() {
			// Set the file lock to be able to close it later.
			this.fileLock = tryOpenWorkingDirectory(profile.getSettings()
				.getWorkingDirectory());
			Boolean workingDirectoryExists = false;
			if (this.fileLock != null) {
				workingDirectoryExists = true;
			}

			if (workingDirectoryExists) {
				progress = new ProgressDialogViewWindow("Connecting to HPC",
					ownerWindow);
				JavaFXRoutines.runOnFxThread(this::initExceptionHandler);
			}
			return workingDirectoryExists;
		}

		void initDone() {
			if (progress != null) {
				progress.done();
			}
		}

		void dispose() {
			JavaFXRoutines.runOnFxThread(this::disposeExceptionHandler);
			if (fileLock != null) {
				fileLock.close();
				fileLock = null;
			}
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
