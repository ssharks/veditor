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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import java.io.*;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

abstract public class NewHdlWizard extends Wizard implements INewWizard
{
	private NewHdlWizardPage page;

	private ISelection selection;

	public NewHdlWizard()
	{
		super();
		setNeedsProgressMonitor(true);
	}

	public void addPages(String extension)
	{
		page = new NewHdlWizardPage(selection, extension);
		addPage(page);
	}

	public boolean performFinish()
	{
		final String containerName = page.getContainerName();
		final String moduleName = page.getModuleName();
		final String fileName = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException
			{
				try
				{
					doFinish(containerName, moduleName, fileName, monitor);
				}
				catch (CoreException e)
				{
					throw new InvocationTargetException(e);
				}
				finally
				{
					monitor.done();
				}
			}
		};
		try
		{
			getContainer().run(true, false, op);
		}
		catch (InterruptedException e)
		{
			return false;
		}
		catch (InvocationTargetException e)
		{
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException
					.getMessage());
			return false;
		}
		return true;
	}
	
	private void doFinish(String containerName, String moduleName,
			String fileName, IProgressMonitor monitor) throws CoreException
	{
		monitor.beginTask("Creating " + fileName, 2);

		// check folder
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer))
		{
			throwCoreException("Container " + containerName
					+ " does not exist.");
		}
		IContainer container = (IContainer) resource;

		// create file
		final IFile file = container.getFile(new Path(fileName));
		if (file.exists())
		{
			throwCoreException("File " + fileName + " already exists.");
		}
		try
		{
			InputStream stream = openContentStream(moduleName);
			file.create(stream, true, monitor);
			stream.close();
		}
		catch (IOException e)
		{
		}
		monitor.worked(1);
		
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable()
		{
			public void run()
			{
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				try
				{
					IDE.openEditor(page, file, true);
				}
				catch (PartInitException e)
				{
				}
			}
		});
		monitor.worked(2);
	}
	
	private InputStream openContentStream(String moduleName)
	{
		String contents = getInitialContents(moduleName);
		return new ByteArrayInputStream(contents.getBytes());
	}
	
	abstract String getInitialContents(String moduleName);

	private void throwCoreException(String message) throws CoreException
	{
		IStatus status = new Status(IStatus.ERROR, "Verilog/VHDL", IStatus.OK,
				message, null);
		throw new CoreException(status);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection)
	{
		this.selection = selection;
	}
}







