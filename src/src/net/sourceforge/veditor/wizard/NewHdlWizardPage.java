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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class NewHdlWizardPage extends WizardPage
{
	private Text containerText;

	private Text moduleText;

	private ISelection selection;
	
	private String extension;

	public NewHdlWizardPage(ISelection selection, String extension)
	{
		super("ModuleWizardPage");
		this.extension = extension;
		
		if (extension.equals(".v"))
		{
			setTitle("Verilog module");
			setDescription("Create a new Verilog module.");
		}
		else
		{
			setTitle("VHDL entity");
			setDescription("Create a new VHDL entity.");
		}
		this.selection = selection;
	}

	public void createControl(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;

		containerText = createText(container, "&Folder:");
		
		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				handleBrowse();
			}
		});

		moduleText = createText(container,"&Module name:");
		moduleText.forceFocus();

		initialize();
		setPageComplete(false);

		ModifyTextListner listener = new ModifyTextListner();
		containerText.addModifyListener(listener);
		moduleText.addModifyListener(listener);

		setControl(container);
	}

	private Text createText(Composite container, String name)
	{
		Label label = new Label(container, SWT.NULL);
		label.setText(name);
		Text text = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(gd);

		return text;
	}
	
	private class ModifyTextListner implements ModifyListener
	{
		public void modifyText(ModifyEvent e)
		{
			dialogChanged();
		}

		private void dialogChanged()
		{
			// check container
			String containerName = getContainerName();
			IResource resource = ResourcesPlugin.getWorkspace().getRoot()
					.findMember(new Path(containerName));
			if (containerName.length() == 0)
			{
				updateStatus("Source folder must be specified");
				return;
			}
			if (resource == null || !resource.exists()
					|| !(resource instanceof IContainer))
			{
				updateStatus("Source folder must exist");
				return;
			}
			if (!resource.isAccessible())
			{
				updateStatus("Project must be writable");
				return;
			}
			IContainer container = (IContainer)resource;

			// check file
			String moduleName = getModuleName();
			String fileName = getFileName();
			IFile file = container.getFile(new Path(fileName));
			if (file.exists())
				updateStatus("Module/Entity already exist");
			else if (moduleName.length() == 0)
				updateStatus("Module/Entity name must be specified");
			else if (fileName.replace('\\', '/').indexOf('/', 1) > 0)
				updateStatus("Module/Entity name must be valid");
			else
				updateStatus(null);
		}

		private void updateStatus(String message)
		{
			setErrorMessage(message);
			setPageComplete(message == null);
		}
	}

	private void initialize()
	{
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource)
			{
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				containerText.setText(container.getFullPath().toString());
			}
		}
	}

	private void handleBrowse()
	{
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select a source folder");
		if (dialog.open() == ContainerSelectionDialog.OK)
		{
			Object[] result = dialog.getResult();
			if (result.length == 1)
			{
				containerText.setText(((Path) result[0]).toString());
			}
		}
	}

	public String getContainerName()
	{
		return containerText.getText();
	}

	public String getModuleName()
	{
		return moduleText.getText();
	}
	
	public String getFileName()
	{
		return getModuleName() + extension;
	}
}



