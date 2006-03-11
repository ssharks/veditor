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

package net.sourceforge.veditor.wizard;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.veditor.builder.HdlNature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class NewProjectWizard extends BasicNewResourceWizard
{
	private WizardNewProjectCreationPage page;

	public NewProjectWizard()
	{
		super();
	}

	public void addPages()
	{
		page = new WizardNewProjectCreationPage("NewProjectWizardPage");
		page.setTitle("Verilog/VHDL Project");
		page.setDescription("Create a new Verilog/VHDL project resource.");
		addPage(page);
	}

	public boolean performFinish()
	{
        IProject newProject = page.getProjectHandle();

		IPath newPath = null;
		if (!page.useDefaults())
			newPath = page.getLocationPath();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProjectDescription description;
		description = workspace.newProjectDescription(newProject.getName());
		description.setLocation(newPath);

		// add HdlNature
		String[] natures = new String[1];
		natures[0] = HdlNature.NATURE_ID;
		description.setNatureIds(natures);

		CreateProjectOperation op;
		op = new CreateProjectOperation(newProject, description);

		try
		{
			getContainer().run(true, true, op);
			return true;
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	private class CreateProjectOperation extends WorkspaceModifyOperation
	{
		private IProject project;
		private IProjectDescription description;
		
		public CreateProjectOperation(IProject project, IProjectDescription description)
		{
			this.project = project;
			this.description = description;
		}
		protected void execute(IProgressMonitor monitor) throws CoreException,
				InvocationTargetException, InterruptedException
		{
			try
			{
				monitor.beginTask("", 2000);

				project.create(description, null);

				monitor.worked(1000);
				if (monitor.isCanceled())
					throw new OperationCanceledException();

				project.open(IResource.BACKGROUND_REFRESH, null);
			}
			finally
			{
				monitor.done();
			}

		}
		
	}
}  





