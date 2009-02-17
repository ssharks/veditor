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
package net.sourceforge.veditor.actions;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.builder.ErrorParser;
import net.sourceforge.veditor.builder.ExternalLauncher;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class CompileAction extends AbstractAction
{
	public CompileAction()
	{
		super("Compile");
	}
	
	 public static boolean isNeedToSaveSet() {
		return VerilogPlugin.getPreferenceBoolean("Compile.SaveBeforeCompile");
	 }
	/**
	 * Checks the save before build flag and saves the current file 
	 * if necessary.
	 */
	private void checkAndSaveEditors(){
		if(isNeedToSaveSet()){
			getEditor().doSave(null);
		}
	}
	
	public void run()
	{
		IFile file = getEditor().getHdlDocument().getFile();
		IContainer parent = file.getParent();
		while (parent instanceof IFolder)
		{
			parent = parent.getParent();
		}
		if (parent instanceof IProject)
		{
			//project = (IProject)parent;
		}
		else
		{
			// maybe never execute
			return;
		}
		
		VerilogPlugin.clear();
		
		VerilogPlugin.deleteMarkers(file);
		IPath path = parent.getLocation();
		
		boolean simulationdirfound = false;
		boolean simdirfound = false;
		
		
		// Each file is compiled in the Modeulsim directory
		// This is equal to $(project_loc)/simulation for Barco-SMD
		//                  $(project_loc)/sim for Barco-MID
		
		if(parent.findMember("simulation")!=null) {
			if(parent.findMember("simulation") instanceof IContainer) {
				simulationdirfound=true;
			}
		}
		if(parent.findMember("sim")!=null) {
			if(parent.findMember("sim") instanceof IContainer) {
				simdirfound=true;
			}
		}	
		
		if(!simulationdirfound && !simdirfound) {
			VerilogPlugin.println("Warning simulation or sim directory not found");
			return;
		}
		
		String simulationdir = simulationdirfound?"simulation":"sim";
		
		VerilogPlugin.println("Compiling file for modelsim");
		VerilogPlugin.println("\tfilename: " + file.getLocation().toString());
		VerilogPlugin.println("\tto: " + path.toString()+"/"+simulationdir+"/work");
		VerilogPlugin.println("");
		
		IContainer workdir = (IContainer)parent.findMember(simulationdir);
		
		String command = VerilogPlugin.getPreferenceString("Compile.command")
				+ " " + file.getLocation().toString();

		checkAndSaveEditors();
        
		ExternalLauncher launchar = new ExternalLauncher(workdir, command);
		launchar.run();
		
		String msg = launchar.getMessage();

		ErrorParser[] parsers = ErrorParser.getParsers();
		for(int i = 0 ; i < parsers.length ; i++)
		{
			parsers[i].parse(parent, msg);
		}

		getEditor().update();
	}
}






