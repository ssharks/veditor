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

import java.util.Map;

import net.sourceforge.veditor.VerilogPlugin;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class SimulateBuilder extends IncrementalProjectBuilder
{
	public static final String BUILDER_ID = "net.sourceforge.veditor.simulateBuilder";

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
	{
		String enableValue = getArg(args, "enable");
		if (!enableValue.equals("true"))
			return null;

		String dir = getArg(args, "work");
		String cmd = getArg(args, "command");
		String argstext = getArg(args, "arguments");
		argstext = argstext.replaceAll("\\\\n", " ");
		String cmdline = cmd + " "  + argstext;
		
		IProject project = getProject();
		IContainer folder;
		if (dir.equals("") || dir.equals("/"))
			folder = project;
		else
			folder = project.getFolder(dir);
		ExternalLauncher launcher = new ExternalLauncher(folder, cmdline);
		
		VerilogPlugin.println("----------------------------------------");
		VerilogPlugin.println("veditor build " + kind + " in " + dir + " : " + cmdline);
		VerilogPlugin.println("----------------------------------------");
		launcher.start();
		while(launcher.isAlive())
		{
			launcher.waitFor(500);
			if (monitor.isCanceled())
			{
				launcher.interrupt();
			}
		}
		
		VerilogPlugin.clearProblemMarker(project);
		
		String parserName = getArg(args, "parser");
		ErrorParser parser = ErrorParser.getParser(parserName);
		if (parser != null)
			parser.parse(folder, launcher.getMessage());
	
		return null;
	}

	private String getArg(Map args, String name)
	{
		Object obj = args.get(name);
		if (obj == null)
			return "";
		else
			return obj.toString();
	}

	protected void clean(IProgressMonitor monitor) throws CoreException
	{
	}
}
