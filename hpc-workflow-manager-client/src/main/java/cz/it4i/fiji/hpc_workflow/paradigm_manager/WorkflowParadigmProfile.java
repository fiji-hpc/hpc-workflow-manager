/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.hpc_workflow.paradigm_manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import cz.it4i.fiji.haas.JobWithDirectorySettings;
import cz.it4i.fiji.hpc_client.HPCClient;
import cz.it4i.fiji.hpc_client.HPCClientException;
import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowJobManager;
import cz.it4i.parallel.paradigm_managers.ParadigmProfileWithSettings;

public class WorkflowParadigmProfile<T extends SettingsWithWorkingDirectory, U extends JobWithDirectorySettings>
	extends
	ParadigmProfileWithSettings<T>
{

	private static final long serialVersionUID = 6843116946587764808L;

	private Class<T> typeOfSettings;

	private Class<? extends HPCClient<U>> typeOfClient;

	public WorkflowParadigmProfile(String profileName, Class<T> typeOfSettings,
		Class<? extends HPCClient<U>> typeOfClient)
	{
		super(HPCWorkflowJobManager.class, profileName);
		this.typeOfSettings = typeOfSettings;
		this.typeOfClient = typeOfClient;
	}

	Class<? extends HPCClient<U>> getTypeOfClient() {
		return typeOfClient;
	}

	HPCClient<U> createHPCClient() {
		try {
			@SuppressWarnings("unchecked")
			Constructor<? extends HPCClient<U>>[] constructors =
				(Constructor<? extends HPCClient<U>>[]) typeOfClient.getConstructors();
			for (Constructor<? extends HPCClient<U>> constructor : constructors) {
				if (constructor.getParameterCount() > 0 && constructor
					.getParameterTypes()[0].isAssignableFrom(getTypeOfSettings()))
				{
					return constructor.newInstance(getSettings());
				}
			}
			throw new HPCClientException("Type " + typeOfClient +
				" does not have constructor with parameter " + typeOfSettings);

		}
		catch (SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException exc)
		{
			throw new HPCClientException(exc);
		}
	}
	
	@Override
	protected Class<T> getTypeOfSettings() {
		return typeOfSettings;
	}

	
}
