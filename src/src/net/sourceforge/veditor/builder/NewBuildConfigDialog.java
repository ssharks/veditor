/*******************************************************************************
 * Copyright (c) 2004, 2006 KOBAYASHI Tadashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ali Ghorashi - initial implementation
 *******************************************************************************/
package net.sourceforge.veditor.builder;

import net.sourceforge.veditor.builder.BuildConfig;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NewBuildConfigDialog extends Dialog {
	private Text m_Name;
	private Combo m_CopyFrom;
	private Button m_UseDefault;
	BuildConfig [] m_Configs;
	boolean m_OkPressed,m_bUseDefault;
	BuildConfig m_SelectedConfig;
	String m_NewName;
	
	public NewBuildConfigDialog(Shell parentShell,BuildConfig []configs) {
		super(parentShell);
		m_Configs=configs;
		m_OkPressed=false;
	}
	
	/**
	 * Creates the dialog area
	 */
	protected Control createDialogArea(Composite parent){
		final int labelWidth=40;		
		final int textWidth=260;
		final int comboWidth=100;
		final int checkButtonWidth=200;
		Composite top=createComposite(parent, 1, SWT.NONE);
		
		Composite composite=createComposite(top, 2, SWT.NONE);
		Label label=new Label(composite,SWT.NONE);
		label.setText("Name");
		label.setLayoutData(new GridData(labelWidth,SWT.DEFAULT));		
		m_Name=new Text(composite,SWT.BORDER | SWT.LEFT);
		m_Name.setLayoutData(new GridData(textWidth,SWT.DEFAULT));
		
		Composite radioSection=createComposite(top, 2, SWT.NONE);
		m_UseDefault=new Button(radioSection,SWT.CHECK);
		m_UseDefault.setLayoutData(new GridData(checkButtonWidth,SWT.DEFAULT));
		m_UseDefault.setText("Use Default Default Configuration");
		m_UseDefault.setSelection(true);
		createNull(radioSection);
		label=new Label(radioSection,SWT.NONE);
		label.setText("Copy Configuation From");
		label.setLayoutData(new GridData(120,SWT.DEFAULT));
		
		m_CopyFrom=new Combo(radioSection,SWT.READ_ONLY);
		m_CopyFrom.setLayoutData(new GridData(comboWidth,SWT.DEFAULT));
		m_CopyFrom.addSelectionListener(new CopyFromListener());
		//fill the combo
		for(BuildConfig buildConfig:m_Configs){
			m_CopyFrom.add(buildConfig.getName());
			m_CopyFrom.setData(buildConfig.getName(), buildConfig);
		}
		
		return top;
	}
	
	
	/**
	 * Greates a composite group
	 * @param parent parent control
	 * @param column number of columns
	 * @param style Additional styles
	 * @return the created composite
	 */
	private Composite createComposite(Composite parent, int column,int style)
	{
		Composite group = new Composite(parent, style);
		group.setLayout(new GridLayout(column, false));
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		group.setLayoutData(gd);
		return group;
	}
	
	/**
	 * creates an empty section used to fill a cell
	 * @param parent
	 */
	private void createNull(Composite parent)
	{
		Composite group = new Composite(parent, SWT.NONE);
		GridData gd = new GridData();
		gd.heightHint = 0;
		gd.widthHint = 0;
		group.setLayoutData(gd);
	}
	
   protected void configureShell(Shell newShell) {
	      super.configureShell(newShell);
	      newShell.setText("Create a new configuraion..");
	   }
   
   /**
    * Called when the OK button is pressed
    */
   protected void okPressed() {
	   m_NewName=m_Name.getText();
	   
	   if( m_CopyFrom.getSelectionIndex()!=-1){
		   m_SelectedConfig=(BuildConfig)m_CopyFrom.getData(m_CopyFrom.getText());		  
	   }
	   else{
		   m_bUseDefault=true;
	   }
	   super.okPressed();
	   m_OkPressed=true;		
	}
   
   /**
    * Returns true if the OK button was pressed
    * @return
    */
   public boolean isOkPressed(){return m_OkPressed;}
   
   /**
    * Called when the cancel button is pressed
    */
   protected void cancelPressed(){
	   super.cancelPressed();
   }
   
   /**
    * Returns a build configuration based on the user input
    * @return
    */
   public BuildConfig getBuildConfig(){
	   BuildConfig results=null;
	   if(m_bUseDefault){
		   //default configuraion
		   results=new BuildConfig();
		   results.setName(m_NewName);
	   }
	   else{
		   //copy a configuration
		   BuildConfig source=m_SelectedConfig.clone();
		   results=source.clone();
		   results.setName(m_NewName);
	   }
	   
	   return results;
   }
   
   private class CopyFromListener extends SelectionAdapter{
	   public void widgetSelected(SelectionEvent e)
		{
		   m_UseDefault.setSelection(false);
		   m_bUseDefault=false;
		}
   }
}
