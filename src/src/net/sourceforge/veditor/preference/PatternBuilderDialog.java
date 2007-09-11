/*******************************************************************************
 * Copyright (c) 2007 Ali Ghorashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ali Ghorashi - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.preference;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PatternBuilderDialog extends Dialog {
	private String m_PatternString;
	private Text m_Pattern;
	private Text m_Sample;
	private Text m_File;
	private Text m_Line;
	private Text m_Message;
	
	public PatternBuilderDialog(Shell parentShell,String pattern) {
		super(parentShell);
		m_PatternString=pattern;
	}
	
	/**
	 * Creates the dialog area
	 */
	protected Control createDialogArea(Composite parent){
		final int labelWidth=60;		
		final int textWidth=260;
		final int multiLineHeight=100;
		final int buttonWidth=60;
		
		Composite top=createComposite(parent, 1, SWT.NONE);
		Label label;
		
		Composite userFields=createComposite(top, 2, SWT.BORDER);
		//pattern
		label=new Label(userFields,SWT.NONE);
		label.setText("Pattern");
		label.setLayoutData(new GridData(labelWidth,SWT.DEFAULT));
		m_Pattern=new Text(userFields,SWT.BORDER | SWT.LEFT);
		m_Pattern.setLayoutData(new GridData(textWidth,SWT.DEFAULT));
		m_Pattern.setText(m_PatternString);
		
		//sample field
		label=new Label(userFields,SWT.NONE);
		label.setText("Sample");
		label.setLayoutData(new GridData(labelWidth,SWT.DEFAULT));
		m_Sample=new Text(userFields,SWT.BORDER | SWT.LEFT | SWT.MULTI | SWT.V_SCROLL|SWT.H_SCROLL);
		m_Sample.setLayoutData(new GridData(textWidth,multiLineHeight));
		
		//check button
		Button button=new Button(top,SWT.PUSH);
		button.setText("Check");
		button.setLayoutData(new GridData(buttonWidth,SWT.DEFAULT));
		button.addSelectionListener(new CheckListener());
		
		label=new Label(top,SWT.NONE);
		label.setText("Results");
		
		Composite results=createComposite(top, 2, SWT.BORDER);
		//File
		label=new Label(results,SWT.NONE);
		label.setText("File");
		label.setLayoutData(new GridData(labelWidth,SWT.DEFAULT));
		m_File=new Text(results,SWT.BORDER | SWT.LEFT|SWT.READ_ONLY);
		m_File.setLayoutData(new GridData(textWidth,SWT.DEFAULT));
		//Line
		label=new Label(results,SWT.NONE);
		label.setText("Line");
		label.setLayoutData(new GridData(labelWidth,SWT.DEFAULT));
		m_Line=new Text(results,SWT.BORDER | SWT.LEFT|SWT.READ_ONLY);
		m_Line.setLayoutData(new GridData(textWidth,SWT.DEFAULT));
		//message
		label=new Label(results,SWT.NONE);
		label.setText("Message");
		label.setLayoutData(new GridData(labelWidth,SWT.DEFAULT));
		m_Message=new Text(results,SWT.BORDER | SWT.LEFT | SWT.MULTI | SWT.H_SCROLL| SWT.V_SCROLL|SWT.READ_ONLY);
		m_Message.setLayoutData(new GridData(textWidth,multiLineHeight));
		
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
	 * Gets the pattern string
	 * @return Pattern string
	 */
	public String getPattern(){
		return m_PatternString;
	}
	
   /**
    * Called when the OK button is pressed
    */
   protected void okPressed() {
	   m_PatternString=m_Pattern.getText();	   
	   super.okPressed();	   		
	}
   
   /**
    * This Class is called when the user 
    *
    */
   private class CheckListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			Pattern pattern = Pattern.compile(m_Pattern.getText());
			Matcher matcher = pattern.matcher(m_Sample.getText());
			if (matcher.matches()) {
				int groupCount=matcher.groupCount();
				if(groupCount>2){
					m_File.setText( matcher.group(groupCount-2));					
				}
				if(groupCount>1){
					m_Line.setText( matcher.group(groupCount-1));					
				}
				if(groupCount>0){
					m_Message.setText( matcher.group(groupCount));					
				}
				
			} else {
				m_Message.setText("Pattern does not match anything in the sample!!");
			}

		}
	}

}
