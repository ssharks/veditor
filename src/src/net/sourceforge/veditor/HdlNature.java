/*******************************************************************************
 * Copyright (c) 2004, 2006 KOBAYASHI Tadashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    KOBAYASHI Tadashi - initial API and implementation
 *******************************************************************************/

package net.sourceforge.veditor;

import java.util.ArrayList;
import java.util.Map;

import net.sourceforge.veditor.builder.BuildConfig;
import net.sourceforge.veditor.builder.SimulateBuilder;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class HdlNature implements IProjectNature
{
	public static final String NATURE_ID = "net.sourceforge.veditor.HdlNature";
	public static final String SIMULATOR_ID = SimulateBuilder.BUILDER_ID;

	private IProject project;
	
	public HdlNature()
	{
		super();
	}
	
	
	/**
	 * This function checks see if a builder already exists. 
	 * If one is not found, then one is created and added
	 * with default parameters
	 * @throws CoreException
	 */
	private void checkAndAddBuilder() throws CoreException{
		ArrayList<ICommand> commandList=new ArrayList<ICommand>();
		ICommand hdlCommand=null;

		IProjectDescription description;
		description = getProject().getDescription();
		for(ICommand command: description.getBuildSpec()){
			//try to find an existing builder in the list of builders
			if(command.getBuilderName().startsWith(SIMULATOR_ID)){
				hdlCommand=command;
			}
			commandList.add(command);
		}				
		//if no hdl command was found
		if(hdlCommand==null){
			//make a new one
			hdlCommand = description.newCommand();
			hdlCommand.setBuilderName(SIMULATOR_ID);
			//add it to the list of builders
			commandList.add(hdlCommand);
			//set the arguments 
			Map<String,String> args=BuildConfig.encodeArgs(new BuildConfig[]{new BuildConfig()});
			hdlCommand.setArguments(args);
			//set the builders
			description.setBuildSpec(commandList.toArray(new ICommand[0]));
			getProject().setDescription(description, null);
		}		
		
	}
		
	/**
	 * Adds an instance of our builder with default values
	 */
	public void configure() throws CoreException
	{
		checkAndAddBuilder();
	}
	
	/**
	 * Removes our builder form the builders list
	 */
	public void deconfigure() throws CoreException
	{
		ArrayList<ICommand> commandList=new ArrayList<ICommand>();

		IProjectDescription description;
		description = getProject().getDescription();
		for(ICommand command: description.getBuildSpec()){
			//try to find an existing builder in the list of builders
			if(command.getBuilderName().startsWith(SIMULATOR_ID)){
				//skip this one
				continue;
			}
			commandList.add(command);
		}				
		//set the builders
		description.setBuildSpec(commandList.toArray(new ICommand[0]));
		getProject().setDescription(description, null);
	}
	
	/**
	 * Gets a list of builder commands and their arguments for this project
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String,BuildConfig> getCommands(){
		Map<String,BuildConfig> buildConfigList=null;
		try
		{
			IProjectDescription description;
			description = getProject().getDescription();
			for(ICommand command: description.getBuildSpec()){
				if(command.getBuilderName().equals(SIMULATOR_ID)){					
					buildConfigList=BuildConfig.parseCommandArguments(command.getArguments());
					break;
				}
			}			
		}
		catch (CoreException e)
		{
		}		
		return buildConfigList;
	}
	
	/**
	 * Sets the commands based on the given arguments
	 */
	public void setCommands(BuildConfig[] buildConfig){
		ArrayList<ICommand> buildConfigList=new ArrayList<ICommand>();
		ICommand hdlCommand=null;
		
		try
		{
			IProjectDescription description;
			description = getProject().getDescription();
			
			//
			// If the user blew away the builder we may need to add it back in
			checkAndAddBuilder();
						
			for(ICommand command: description.getBuildSpec()){
				//try to find an existing builder in the list of builders
				if(command.getBuilderName().startsWith(SIMULATOR_ID)){
					hdlCommand=command;
				}
				buildConfigList.add(command);
			}				
						
			if(hdlCommand!=null){
				//set the arguments 
				Map<String,String> args=BuildConfig.encodeArgs(buildConfig);
				hdlCommand.setArguments(args);
			}				
			//set the builders
			description.setBuildSpec(buildConfigList.toArray(new ICommand[0]));
			getProject().setDescription(description, null);
		}
		catch (CoreException e)
		{
		}	
	}
	

	public IProject getProject()
	{
		return project;
	}

	public void setProject(IProject project)
	{
		this.project = project;
	}
	
}
