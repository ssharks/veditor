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

package net.sourceforge.veditor.wizard;

import java.util.LinkedList;
import java.util.Stack;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.editor.HdlSourceViewerConfiguration;
import net.sourceforge.veditor.editor.VerilogEditor;
import net.sourceforge.veditor.editor.VhdlEditor;
import net.sourceforge.veditor.templates.VerilogNewFileContext;
import net.sourceforge.veditor.templates.VhdlNewFileContext;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class NewHdlWizardPage extends WizardPage
{
	private Text containerText;
	private Text moduleText;
	private ISelection selection;
	private String extension;
	private CCombo				templateCombo;	
	private Text				briefText;
	private Text				templateDescription;
	private SourceViewer		templatePreview;	
	private LinkedList <Template>	newFileTemplates	= new LinkedList <Template>();
	private enum LanguageMode {verilog,vhdl};
	private LanguageMode languageMode;


	public NewHdlWizardPage(ISelection selection, String extension)
	{
		super("ModuleWizardPage");
		this.extension = extension;
		
		if (extension.equals(".v"))
		{
			setTitle("Verilog module");
			setDescription("Create a new Verilog module.");
			languageMode=LanguageMode.verilog;
		}
		else
		{
			setTitle("VHDL entity");
			setDescription("Create a new VHDL entity.");
			languageMode=LanguageMode.vhdl;
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
		
		//mg
		GridData data = new GridData( GridData.GRAB_HORIZONTAL
				+ GridData.FILL_HORIZONTAL );
		data.horizontalSpan = 2;
		data.minimumHeight = 20;		
		
		moduleText.setLayoutData( data );

		//brief
		briefText = createText( container, "&Brief description:" );
		briefText.setLayoutData( data );

		//template combo	
		Template[] templates;
		switch(languageMode){
		case verilog:
			templates = VerilogPlugin.getPlugin().getTemplateStore().getTemplates(VerilogNewFileContext.CONTEXT_TYPE);
			break;
		default:
		case vhdl:
			templates = VerilogPlugin.getPlugin().getTemplateStore().getTemplates(VhdlNewFileContext.CONTEXT_TYPE);
			break;			
		}		

		Stack<Template> newFileTemplatesStack = new Stack<Template>();

		for (Template template : templates) {
			newFileTemplatesStack.add(template);
		}		
		
		while ( !newFileTemplatesStack.isEmpty() ) {
			newFileTemplates.add( newFileTemplatesStack.pop() );
		}

		templateCombo = createCombo( container, "Available templates:",
				newFileTemplates );
		templateCombo.setLayoutData( data );
		templateCombo.select( 0 );
		
		//template description
		templateDescription = createText( container, "&Description" );
		templateDescription.setLayoutData( data );
		templateDescription.setBackground( new Color( Display.getCurrent(),
				255, 255, 255 ) );
		templateDescription.setEditable( false );

		//template preview
		GridData previewData = new GridData( GridData.GRAB_HORIZONTAL
				+ GridData.FILL_HORIZONTAL );
		previewData.horizontalSpan = 3;
		
		templatePreview = createEditor( container, "editor" );
		templatePreview.getTextWidget().setLayoutData( previewData );

		//initialize description and preview
		try {
			int templateSelectedIndex = templateCombo.getSelectionIndex();
			Template templateSelected = newFileTemplates
					.get( templateSelectedIndex );

			templateDescription.setText( templateSelected.getDescription() );
			templatePreview.getDocument().set( templateSelected.getPattern() );
		} catch ( IndexOutOfBoundsException e ) {
		}

		//mg------------------------

		initialize();
		setPageComplete(false);

		ModifyTextListner listener = new ModifyTextListner();
		containerText.addModifyListener(listener);
		moduleText.addModifyListener(listener);

		//mg
		briefText.addModifyListener( listener );
		templateCombo.addSelectionListener( new SelectionListener() {

			public void widgetSelected( SelectionEvent e ) {

				try {
					final Template templateSelected = getTemplate();

					templateDescription.setText( templateSelected
							.getDescription() );
					templatePreview.getDocument().set( templateSelected.getPattern() );
				} catch ( Exception exception ) {
				}
			}

			public void widgetDefaultSelected( SelectionEvent e ) {
			}

		} );
		//mg-------------------------

		
		setControl(container);
	}

	//mg
	private CCombo createCombo( final Composite container, final String name,
			final LinkedList <Template> templates ) {
		final Label label = new Label( container, SWT.NULL );
		label.setText( name );
		final CCombo combo = new CCombo( container, SWT.NONE );

		for ( final Template template : templates ) {
			combo.add( template.getName() );
		}

		return combo;
	}
	
	private SourceViewer createEditor(Composite parent, String pattern) {
		SourceViewer viewer= createViewer(parent);
		viewer.setEditable(true);

		IDocument document= viewer.getDocument();
		if (document != null)
			document.set(pattern);
		else {
			document= new Document(pattern);
			viewer.setDocument(document);
		}

		int nLines= document.getNumberOfLines();
		if (nLines < 5) {
			nLines= 5;
		} else if (nLines > 12) {
			nLines= 12;
		}

		Control control= viewer.getControl();
		GridData data= new GridData(GridData.FILL_BOTH);
		data.widthHint= convertWidthInCharsToPixels(80);
		data.heightHint= convertHeightInCharsToPixels(nLines);
		control.setLayoutData(data);

		return viewer;
	}
	
	protected SourceViewer createViewer(Composite parent) {
		SourceViewer viewer= new SourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		SourceViewerConfiguration configuration;
		
		switch(languageMode){
		case verilog:
			configuration = HdlSourceViewerConfiguration.createForVerilog( new VerilogEditor() );
			break;
		default:
		case vhdl:
			configuration = HdlSourceViewerConfiguration.createForVhdl( new VhdlEditor() );
			break;			
		}		
		
		viewer.configure(configuration);
		return viewer;
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

			// check module name
			String moduleName = getModuleName();
			if (Character.isLetter(moduleName.charAt(0)) == false)
			{
				updateStatus("Module/Entity name must start a letter");
				return;
			}
			for (int i = 1; i < moduleName.length(); i++)
			{
				if (Character.isJavaIdentifierPart(moduleName.charAt(i)) == false)
				{
					updateStatus("Module/Entity name must be identifier");
					return;
				}
			}
			
			// check file
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
	//mg
	public String getBrief() {
		return briefText.getText();
	}
	
	public String getTemplatePattern() {
		return templatePreview.getDocument().get();
	}

	public Template getTemplate() {
		int templateSelectedIndex = templateCombo.getSelectionIndex();

		try {
			return newFileTemplates.get( templateSelectedIndex );
		} catch ( IndexOutOfBoundsException exception ) {
			return null;
		}
	}
	//mg----------------------

}



