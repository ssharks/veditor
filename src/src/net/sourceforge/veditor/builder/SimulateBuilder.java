//
//  Copyright 2004, 2006, KOBAYASHI Tadashi
//  $Id$
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//

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
		String enableValue = args.get("enable").toString();
		if (!enableValue.equals("true"))
			return null;

		String dir = args.get("work").toString();
		String cmd = args.get("command").toString();
		String argstext = args.get("arguments").toString(); 
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
		
		AbstractMessageParser[] parsers = MessageParserFactory.createAll();
		for(int i = 0; i < parsers.length; i++)
		{
			if (parsers[i].parse(folder, launcher.getMessage()))
				break;
		}
	
		return null;
	}

	protected void clean(IProgressMonitor monitor) throws CoreException
	{
	}
}
