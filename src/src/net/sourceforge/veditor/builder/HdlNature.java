//
//  Copyright 2004, 2006 KOBAYASHI Tadashi
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

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class HdlNature implements IProjectNature
{
	public static final String NATURE_ID = "net.sourceforge.veditor.hdlNature";
	public static final String SIMLATOR_ID = SimulateBuilder.BUILDER_ID;

	private IProject project;
	
	public HdlNature()
	{
		super();
	}
	
	public HdlNature(IProject project)
	{
		super();
		this.project = project;
	}

	public void configure() throws CoreException
	{
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();

		if (getCommandIndex(commands) >= 0)
			return;

		ICommand newCommand = createSimulateCommand();
		commands = addCommand(commands, newCommand);

		description.setBuildSpec(commands);
		project.setDescription(description, null);
	}

	public void deconfigure() throws CoreException
	{
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		int idx = getCommandIndex(commands);
		if (idx >= 0)
		{
			ICommand[] newCommands = new ICommand[commands.length - 1];
			System.arraycopy(commands, 0, newCommands, 0, idx);
			System.arraycopy(commands, idx + 1, newCommands, idx,
					commands.length - idx - 1);
			description.setBuildSpec(newCommands);
		}
	}
	
	public ICommand getSimulateCommand()
	{
		try
		{
			IProjectDescription description;
			description = getProject().getDescription();
			ICommand[] commands = description.getBuildSpec();
			int idx = getCommandIndex(commands);
			if (idx >= 0)
				return commands[idx];
		}
		catch (CoreException e)
		{
		}
		return null;
	}
	
	public void setSimulateCommand(ICommand newCommand)
	{
		try
		{
			IProjectDescription description;
			description = getProject().getDescription();
			ICommand[] commands = description.getBuildSpec();
			int idx = getCommandIndex(commands);
			if (idx >= 0)
				commands[idx] = newCommand;
			else
				commands = addCommand(commands, newCommand);
			description.setBuildSpec(commands);
			getProject().setDescription(description, null);
		}
		catch (CoreException e)
		{
		}
	}
	
	public ICommand createSimulateCommand()
	{
		try
		{
			IProjectDescription description;
			description = getProject().getDescription();
			ICommand newCommand = description.newCommand();
			newCommand.setBuilderName(SIMLATOR_ID);
			return newCommand;
		}
		catch (CoreException e)
		{
		}
		return null;
	
	}
	
	private int getCommandIndex(ICommand[] commands)
	{
		for (int i = 0; i < commands.length; ++i)
		{
			if (commands[i].getBuilderName().equals(SIMLATOR_ID))
				return i;
		}
		return -1;
	}
	
	private ICommand[] addCommand(ICommand[] commands, ICommand newCommand)
	{
		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		newCommands[newCommands.length - 1] = newCommand;
		return newCommands;
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

