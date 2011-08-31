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

import java.io.File;

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
	protected String commandString = "Compile.command";
	
	public CompileAction()
	{
		super("Compile");
	}
	
	public CompileAction(String name)
	{
		super(name);
	}
	
	 public static boolean isNeedToSaveSet() {
		return VerilogPlugin.getPreferenceBoolean("Compile.SaveBeforeCompile");
	 }
	/**
	 * Checks the save before build flag and saves the current file 
	 * if necessary.
	 */
	private void checkAndSaveEditors(){
		if(isNeedToSaveSet() && getEditor().isDirty()){
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
		
		VerilogPlugin.deleteExternalMarkers(file);
		IPath path = parent.getLocation();
		
		String simulationdir = VerilogPlugin.getPreferenceString("Compile.Folder");
		
		if(simulationdir.length() != 0 && !(new File(path.toString()+"/"+simulationdir).exists()))
		{			
			VerilogPlugin.println("Warning directory \"" + path.toString()+"/"+simulationdir + "\" not found");
			return;
		}
		
		IContainer workdir = (IContainer)parent.findMember(simulationdir);
		
		if(workdir==null) {
			VerilogPlugin.println("Warning directory \"" + path.toString()+"/"+simulationdir + "\""+
					" exists on file system, but not in your project, try refreshing your project");
			return;
		}
		
		/*
		 * %f - filename
		 * %p - path
		 * %w - workspace path
		 * %d - path relative to workspace path
		 * %s - simulation dir
		 */
		
		String command = VerilogPlugin.getPreferenceString(commandString);
		String temp = file.getLocation().toString().replace(file.getName(), "");
		
		command = command.replace("%f", file.getName());
		command = command.replace("%p", temp);
		command = command.replace("%s", simulationdir);		
		command = command.replace("%w", path.toString());
		temp = temp.replace(path.toString(), "");
		command = command.replace("%d", temp.substring(1,temp.length()));
		
		VerilogPlugin.println("Compiling: " + file.getLocation().toString());
		VerilogPlugin.println("       in: " + path.toString()+ (simulationdir.length()==0?"":"/") +simulationdir);
		VerilogPlugin.println("");
		VerilogPlugin.println("  Command: " + command + "\n");

		checkAndSaveEditors();
        
		ExternalLauncher launchar = new ExternalLauncher(workdir, command);
		launchar.run();

		getEditor().update();
	}
}






