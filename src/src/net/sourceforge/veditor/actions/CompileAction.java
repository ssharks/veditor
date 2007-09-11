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
		IContainer folder = parent;
		
		String command = VerilogPlugin.getPreferenceString("Compile.command")
				+ " " + file.getName();

		
		checkAndSaveEditors();
        
		ExternalLauncher launchar = new ExternalLauncher(folder, command);
		launchar.run();
		
		String msg = launchar.getMessage();

		ErrorParser[] parsers = ErrorParser.getParsers();
		for(int i = 0 ; i < parsers.length ; i++)
		{
			parsers[i].parse(folder, msg);
		}

		getEditor().update();
	}
}






