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

package net.sourceforge.veditor.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import net.sourceforge.veditor.HdlNature;
import net.sourceforge.veditor.VerilogPlugin;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class SimulateBuilder extends IncrementalProjectBuilder
{
	public static final String BUILDER_ID = "net.sourceforge.veditor.simulateBuilder";

	@SuppressWarnings("unchecked")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
	{
		Map<String,BuildConfig> buildConfigs=BuildConfig.parseCommandArguments(args);
		IProject project = getProject();
		IContainer folder;
		boolean bInterrupted=false;
		
		VerilogPlugin.deleteExternalMarkers(project);
		ArrayList<String> keyList=new ArrayList<String>();
		keyList.addAll(buildConfigs.keySet());
		Collections.sort(keyList);
		//set the bounds for the progress monitor
		monitor.beginTask("Building HDL Files", keyList.size());
		for(String name : keyList.toArray(new String[0])){
			BuildConfig buildConfig=buildConfigs.get(name);
			
			//is this configuration enabled?
			if(buildConfig.isEnabled() == false){
				continue;
			}
			monitor.subTask("Building Configuration: "+buildConfig.getName());

			//get the real path
			if (buildConfig.getWorkFolder().equals("") || 
					buildConfig.getWorkFolder().equals("./") || 
					buildConfig.getWorkFolder().equals(".\\")){
				folder = project;
			}
			else{
				folder = project.getFolder(buildConfig.getWorkFolder());
			}
			
			VerilogPlugin.deleteExternalMarkers(project);
			
			//print a banner
			VerilogPlugin.clear();		
			ErrorParser.installParser(buildConfig.getParser(),project);
			
			VerilogPlugin.println("----------------------------------------");
			VerilogPlugin.println("veditor using \"" + buildConfig.getName() + 
					"\" in \"" 
					+ folder.getLocation().toOSString() 
					+ "\" : " 
					+  buildConfig.getCommand());
			VerilogPlugin.println("----------------------------------------");
			//kick off the launcher
			ExternalLauncher launcher = new ExternalLauncher(folder, buildConfig.m_Command);
			launcher.start();
			//monitor launcher
			while(launcher.isAlive())
			{
				launcher.waitFor(500);
				if (monitor.isCanceled())
				{
					launcher.interrupt();
					bInterrupted=true;
					break;
				}
			}
			//if the user killed the launcher
			if(bInterrupted){
				break;
			}
			monitor.worked(1);
		}
		monitor.done();
		//for now we don't do delta builds
		forgetLastBuiltState();
		return null;
	}
	
	/**
	 * Performs the clean operations
	 */
 	protected void clean(IProgressMonitor monitor) throws CoreException
	{
		Map<String,BuildConfig> buildConfigs=null;
		IProject project = getProject();
		IContainer folder;
		boolean bInterrupted=false;
		
		HdlNature nature;
		try {
			//here, we hope there is a project nature
			nature = (HdlNature)project.getNature(HdlNature.NATURE_ID);			
		} catch (CoreException e) {
			e.printStackTrace();
			return;
		}
		 
		//if there is no nature, bail 
		if(nature == null){
			return;
		}
		
		buildConfigs=nature.getCommands();
		
		VerilogPlugin.deleteExternalMarkers(project);
		ArrayList<String> keyList=new ArrayList<String>();
		keyList.addAll(buildConfigs.keySet());
		Collections.sort(keyList);
		//set the bounds for the progress monitor
		monitor.beginTask("Cleaning HDL Files", keyList.size());
		for(String name : keyList.toArray(new String[0])){
			BuildConfig buildConfig=buildConfigs.get(name);
			
			//is this configuration enabled?
			if(buildConfig.isEnabled() == false){
				continue;
			}
			monitor.subTask("Cleaning Configuration: "+buildConfig.getName());

			//get the real path
			if (buildConfig.getWorkFolder().equals("") || 
					buildConfig.getWorkFolder().equals("./") || 
					buildConfig.getWorkFolder().equals(".\\")){
				folder = project;
			}
			else{
				folder = project.getFolder(buildConfig.getWorkFolder());
			}
			//print a banner			
			VerilogPlugin.println("----------------------------------------");
			VerilogPlugin.println("veditor clean operation \"" + buildConfig.getName() + 
					"\" in \"" 
					+ folder.getLocation().toOSString() 
					+ "\" : " 
					+  buildConfig.getCleanCommand());
			VerilogPlugin.println("----------------------------------------");
			//kick off the launcher
			ExternalLauncher launcher = new ExternalLauncher(folder, buildConfig.getCleanCommand());
			launcher.start();
			//monitor launcher
			while(launcher.isAlive())
			{
				launcher.waitFor(500);
				if (monitor.isCanceled())
				{
					launcher.interrupt();
					bInterrupted=true;
					break;
				}
			}			
			//if the user killed the launcher
			if(bInterrupted){
				break;
			}
			monitor.worked(1);
		}
		monitor.done();

	}
}
