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
package net.sourceforge.veditor.preference;

import net.sourceforge.veditor.builder.SimulatorPropertyPage;

import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Top Preference page
 */
public class TopPreferencePage extends AbstractSimplePreferencePage
{
	public TopPreferencePage()
	{
	}

	protected void createFieldEditors()
	{
		addBooleanField(PreferenceStrings.CONTENT_ASSIST_MODULE_PARAM,
				"Generate module parameter with instantiation(Verilog-2001)");
		addBooleanField(PreferenceStrings.SCAN_ENABLE, "Enable Scan Project");
		addStringField(PreferenceStrings.MAX_PARSE_TIME,"Max amount time spent scanning files (mS)");
		addStringField(PreferenceStrings.MAX_PARSE_LINES,"Maximum number of lines in a file to scan");
		addBooleanField(PreferenceStrings.SORT_OUTLINE, "Sort in Outline/Hierarchy");
		addBooleanField(PreferenceStrings.FILTER_SINGALS_IN_OUTLINE, "Filter Signals in Outline");
		addBooleanField(PreferenceStrings.FILTER_PORTS_IN_OUTLINE, "Filter Ports in Outline");
		addBooleanField(PreferenceStrings.MARK_SELECTION_OCCURENCES,"Mark occurences of the selected text");
		addBooleanField(PreferenceStrings.SAVE_BEFORE_COMPILE,"Save File Before Compile");
		
		//mg
//		addStringField(PreferenceStrings.COMPILE_COMMAND, "Compile command");
//		addStringField(PreferenceStrings.SYNTH_COMMAND, "Synthesize command");
//		addStringField(PreferenceStrings.COMPILE_FOLDER, "Compile folder");
		addCommandField( PreferenceStrings.COMPILE_COMMAND, "Compile command" );
		addCommandField( PreferenceStrings.SYNTH_COMMAND, "Synthesize command" );
		addCommandField( PreferenceStrings.COMPILE_FOLDER, "Compile folder" );
		//mg-----------------------

	}

	//mg
	private void addCommandField( final String name, final String label ) {
		final StringFieldEditor comp = new StringFieldEditor( name, label,
				getFieldEditorParent() );
		addField( comp );

		final Button button = new Button( getFieldEditorParent(), SWT.NONE );
		button.addSelectionListener( new SelectionAdapter() {

			public void widgetSelected( final SelectionEvent e ) {
				final StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getShell() );
				dialog.open();
				final String var = dialog.getVariableExpression();
				comp.getTextControl( getFieldEditorParent() ).insert( var );
			}
		} );
		button.setText( "Variables..." );
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = SWT.END;
		button.setLayoutData( data ); 
	}
	//mg-------------

	public void init(IWorkbench workbench)
	{
	}

}

