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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;

import net.sourceforge.veditor.HdlNature;
import net.sourceforge.veditor.builder.BuildConfig;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.dialogs.PropertyPage;

public class SimulatorPropertyPage extends PropertyPage
{
	static final int CONFIG_NAME_COL_IDX=1;
	private Text m_BuildCommand,m_CleanCommand;
	private Text m_WorkFolder,m_Name;
	private Combo m_ErrorParser;
	private Table m_ConfigTable;

	
	private void updateTableItem(TableItem item){
		BuildConfig buildConfig=(BuildConfig)item.getData();
		item.setChecked(buildConfig.isEnabled());
		item.setText(CONFIG_NAME_COL_IDX,buildConfig.getName());		
	}
	/**
	 * Creates and returns a new table item in the configuration table
	 */
	private TableItem createTableItem(BuildConfig buildConfig){
		TableItem item=new TableItem(m_ConfigTable,SWT.NONE,buildConfig.getBuildOrder());		
		item.setData(buildConfig);
		updateTableItem(item);
		return item;
	}
	/**
	 * Creates the config table
	 * @param parent
	 */
	private void createTable(Composite parent){
		GridData gd;
		final int tableWidth=250;
		final int tableHight=150;
		final int enableColumnWidth=60;
		
		Label label=new Label(parent,SWT.NONE);
		label.setText("Configurations");
		m_ConfigTable=new Table (parent, 
				SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL|SWT.CHECK);
		gd = new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL);
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint=tableWidth;
		gd.heightHint=tableHight;
		m_ConfigTable.setLayoutData(gd);
		TableColumn column=new TableColumn(m_ConfigTable,SWT.CENTER);
		column.setText("Enabled");
		column.setWidth(enableColumnWidth);
		column=new TableColumn(m_ConfigTable,SWT.LEFT);
		column.setText("Configuration Name");
		column.setWidth(tableWidth-enableColumnWidth);
		m_ConfigTable.setHeaderVisible(true);
		m_ConfigTable.setLinesVisible(true);
		m_ConfigTable.addSelectionListener(new ConfigTableSelectionListener());
	}
	
	private void createUpper(Composite parent){
		GridData gd;
		final int buttonWidth=80;
		final int buttonHeight=SWT.DEFAULT;
		
		Composite composite=createGroup(parent,2,SWT.BORDER);
		////////////////////
		//table area
		Composite leftSide=new Composite(composite,SWT.NONE);
		leftSide.setLayout(new GridLayout(1,false));
		////////////////////
		//table
		createTable(leftSide);
		////////////////////
		//buttons area
		Composite buttons=new Composite(composite,SWT.NONE);
		buttons.setLayout(new GridLayout(1,false));
		////////////////////
		//new button
		Button button=new Button(buttons,SWT.PUSH);		
		button.setText("New");
		gd = new GridData(buttonWidth,buttonHeight);				
		button.setLayoutData(gd);
		button.addSelectionListener(new NewConfigurationListener());
		////////////////////
		//delete button
		button=new Button(buttons,SWT.PUSH);
		button.setText("Delete");
		button.setLayoutData(gd);
		button.addSelectionListener(new DeleteConfigurationListener());
		/////////////////////
		//up button
		button=new Button(buttons, SWT.ARROW | SWT.UP);
		button.setLayoutData(gd);
		button.addSelectionListener(new UpListener());
		////////////////////
		//down button
		button=new Button(buttons, SWT.ARROW | SWT.DOWN);
		button.setLayoutData(gd);
		button.addSelectionListener(new DownListener());
	}
	
	
	private void createLower(Composite parent){
		final int labelWidth=100;
		final int textWidth=100;
		Label label;
		Button button;
		
		GridData gd;
		Composite composite=createGroup(parent,1,SWT.BORDER);		
		//////////////////////
		//Name
		Composite row1=createGroup(composite,2,SWT.NONE);
		label=new Label(row1,SWT.NONE);
		label.setText("Name");
		label.setLayoutData(new GridData(labelWidth,SWT.DEFAULT));
		m_Name=new Text(row1,SWT.SINGLE | SWT.BORDER);
		m_Name.setLayoutData(new GridData(textWidth,SWT.DEFAULT));
		m_Name.addFocusListener(new FieldsFocusListener());
		m_Name.addKeyListener(new NameInputListener());
		//////////////////////
		//Working folder
		Composite row2=createGroup(composite,3,SWT.NONE);		
		label=new Label(row2,SWT.NONE);
		label.setText("Working Folder");
		gd = new GridData();
		gd.widthHint=labelWidth;
		label.setLayoutData(gd);
		m_WorkFolder= new Text(row2,SWT.SINGLE | SWT.BORDER);
		gd = new GridData();
		gd.widthHint=textWidth;
		m_WorkFolder.setLayoutData(gd);
		m_WorkFolder.addFocusListener(new FieldsFocusListener());
		button= new Button(row2,SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new BrowseListener());
		row2.pack();		
		//////////////////////
		//Parser
		Composite row3=createGroup(composite,2,SWT.NONE);	
		label=new Label(row3,SWT.NONE);
		label.setText("Parser");		
		gd = new GridData();
		gd.widthHint=labelWidth;
		label.setLayoutData(gd);
		m_ErrorParser= new Combo(row3,SWT.READ_ONLY);
		m_ErrorParser.addFocusListener(new FieldsFocusListener());
		gd = new GridData();
		gd.widthHint=textWidth;
		m_ErrorParser.setLayoutData(gd);
		row3.pack();		
		//////////////////////
		//Command
		Composite row4=createGroup(composite,2,SWT.NONE);	
		label=new Label(row4,SWT.NONE);
		label.setText("Command");		
		gd = new GridData();
		gd.widthHint=labelWidth;
		label.setLayoutData(gd);
		m_BuildCommand = new Text(row4, SWT.BORDER);
		m_BuildCommand.addFocusListener(new FieldsFocusListener());
		gd = new GridData();
		gd.widthHint=textWidth*2;
		m_BuildCommand.setLayoutData(gd);
		//////////////////////
		//Clean Command
		Composite row5=createGroup(composite,2,SWT.NONE);	
		label=new Label(row5,SWT.NONE);
		label.setText("Clean Command");		
		gd = new GridData();
		gd.widthHint=labelWidth;
		label.setLayoutData(gd);
		m_CleanCommand = new Text(row5, SWT.BORDER);
		m_CleanCommand.addFocusListener(new FieldsFocusListener());
		gd = new GridData();
		gd.widthHint=textWidth*2;
		m_CleanCommand.setLayoutData(gd);
		
		
		composite.pack();
	}
	
	protected Control createContents(Composite parent)
	{
		Composite top;
		
		top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout(1, false));
		
		createUpper(top);
		createLower(top);
		top.pack();
	    
		initContents();
        return top;
	}
	
	

	
	private Composite createGroup(Composite parent, int column,int style)
	{
		Composite group = new Composite(parent, style);
		group.setLayout(new GridLayout(column, false));
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		group.setLayoutData(gd);
		return group;
	}
	
	private IProject getProject()
	{
		IAdaptable element = getElement();
		if (element != null && element instanceof IProject)
			return (IProject)element;
		else
			return null;
	}
	private String getProjectPath()
	{
		return "/" + getProject().getName();
	}
	
	/**
	 * Initializes the contents of the dialog
	 */
	private void initContents()
	{
		HdlNature nature;
		try {
			nature = getNature();
		} catch (CoreException e) {
			e.printStackTrace();
			return;
		}
		
		ErrorParser[] parsers = ErrorParser.getParsers();
		for(int i = 0; i < parsers.length; i++)
		{
			m_ErrorParser.add(parsers[i].getCompilerName());
		}
		 
		Map<String,BuildConfig> commands=nature.getCommands();
		//if the list is empty, create a command with default settings
		if(commands==null || commands.size() == 0){
			setDefaults();
		}
		else{			
			ArrayList<String> keyList=new ArrayList<String>();
			keyList.addAll(commands.keySet());
			Collections.sort(keyList);
			for(String name : keyList.toArray(new String[0])){
				createTableItem(commands.get(name));
			}
		}		
	}
	
	/**
	 * Updates the selected command with the values in the fields
	 */
	private void saveFields(){
		TableItem[] selectedItems=m_ConfigTable.getSelection();
		
		//there should be only one selected item
		if(selectedItems.length > 0){
			BuildConfig buildConfig=(BuildConfig)selectedItems[0].getData();
			buildConfig.setName(m_Name.getText());
			buildConfig.setCommand(m_BuildCommand.getText());
			buildConfig.setCleanCommand(m_CleanCommand.getText());
			buildConfig.setParser(m_ErrorParser.getText());
			buildConfig.setWorkFolder(m_WorkFolder.getText());			
		}
	}
	/**
	 * Saves the data to the nature
	 */
	private void saveData(){
		
		HdlNature nature;
		try {
			nature = getNature();
		} catch (CoreException e) {			
			e.printStackTrace();
			return;
		}
				
		Vector<BuildConfig> commands=new Vector<BuildConfig>();
		int buildOrder=0;
		saveFields();
		for(TableItem tableItem:m_ConfigTable.getItems()){
			BuildConfig command=(BuildConfig)tableItem.getData();
			command.setBuildOrder(buildOrder++);
			command.setEnabled(tableItem.getChecked());
			commands.add(command);
		}
			
		nature.setCommands(commands.toArray(new BuildConfig[0]));
	}
	
	/**
	 * Sets the default values
	 */
	private void setDefaults(){
//		BuildConfig command=new BuildConfig();
//		createTableItem(command);
	}

	/**
	 * Creates or retrieves a project's nature
	 * @return
	 * @throws CoreException 
	 */
	private HdlNature getNature() throws CoreException {
		IProject project=getProject();		
		HdlNature hdlNature=null;
		IProjectDescription projectDescription;	
		projectDescription = project.getDescription();	
		boolean bNatureFound=false;
		ArrayList<String> natureList=new ArrayList<String>();
			
		for(String nature:projectDescription.getNatureIds()){
			if(HdlNature.NATURE_ID.equals(nature)){
				bNatureFound=true;
				continue;
			}
			natureList.add(nature);
		}
		
		if(bNatureFound==false){
			//add the nature
			natureList.add(HdlNature.NATURE_ID);
			projectDescription.setNatureIds(natureList.toArray(new String[0]));
			project.setDescription(projectDescription, null);			
		}

		hdlNature=(HdlNature) project.getNature(HdlNature.NATURE_ID);
		return hdlNature;		
	}
	
	/**
	 * Called when the user clicks OK
	 */
	public boolean performOk()
	{
		saveData();
		super.performOk();		
		return true;
	}
	
	/**
	 * Called when the user clicks apply
	 */	
	 protected void performApply() {
		 saveData();
	 }
	 
	 /**
	  * Called when the user clicks defaults
	  */
	protected void performDefaults()
	{
		super.performDefaults();
		m_ConfigTable.clearAll();
		setDefaults();		
	}
	
	/**
	 * Class called when the user presses the new button
	 *
	 */
	private class NewConfigurationListener extends SelectionAdapter{
		public void widgetSelected(SelectionEvent e)
		{
			ArrayList<BuildConfig> buildConfigList=new ArrayList<BuildConfig>();
			TableItem []tableItems=m_ConfigTable.getItems();
			
			//gather a list of items
			for(TableItem item:tableItems){
				buildConfigList.add((BuildConfig)item.getData());
			}
			
			NewBuildConfigDialog dialog=new NewBuildConfigDialog(getControl().getShell(),
					buildConfigList.toArray(new BuildConfig[0]));
			dialog.open();
			if(dialog.isOkPressed()){
				createTableItem(dialog.getBuildConfig());
			}
		}
	}
	
	/**
	 * Class called when the user presses the delete button
	 * @author gho18481
	 *
	 */
	private class DeleteConfigurationListener extends SelectionAdapter{
		public void widgetSelected(SelectionEvent e)
		{
			int selectedIdx=m_ConfigTable.getSelectionIndex();		
			//there should be only one selected item
			if(selectedIdx != -1){			
				m_ConfigTable.remove(selectedIdx);
			}
		}
	}
	
	/**
	 * Class called when the user presses the delete button
	 * @author gho18481
	 *
	 */
	private class UpListener extends SelectionAdapter{
		public void widgetSelected(SelectionEvent e)
		{
			int selectedIdx=m_ConfigTable.getSelectionIndex();		
			//there should be only one selected item
			if(selectedIdx > 0 ){			
				TableItem upperItem=m_ConfigTable.getItem(selectedIdx-1);
				TableItem lowerItem=m_ConfigTable.getItem(selectedIdx);
				BuildConfig upperConfig=(BuildConfig)upperItem.getData();
				BuildConfig lowerConfig=(BuildConfig)lowerItem.getData();
				
				m_ConfigTable.setRedraw(false);
				upperConfig.setBuildOrder(selectedIdx);
				lowerConfig.setBuildOrder(selectedIdx-1);				
				createTableItem(upperConfig);
				createTableItem(lowerConfig);
				upperItem.dispose();
				lowerItem.dispose();
				m_ConfigTable.setRedraw(true);
				m_ConfigTable.setSelection(selectedIdx-1);
				m_ConfigTable.setFocus();
			}
		}
	}
	
	/**
	 * Class called when the user presses the delete button
	 * @author gho18481
	 *
	 */
	private class DownListener extends SelectionAdapter{
		public void widgetSelected(SelectionEvent e)
		{
			int selectedIdx=m_ConfigTable.getSelectionIndex();		
			//as long at its not the last one
			if(selectedIdx!= -1 && selectedIdx < m_ConfigTable.getItemCount()-1){			
				TableItem upperItem=m_ConfigTable.getItem(selectedIdx);
				TableItem lowerItem=m_ConfigTable.getItem(selectedIdx+1);
				BuildConfig upperConfig=(BuildConfig)upperItem.getData();
				BuildConfig lowerConfig=(BuildConfig)lowerItem.getData();
				
				m_ConfigTable.setRedraw(false);
				upperConfig.setBuildOrder(selectedIdx+1);
				lowerConfig.setBuildOrder(selectedIdx);				
				createTableItem(upperConfig);
				createTableItem(lowerConfig);
				upperItem.dispose();
				lowerItem.dispose();
				m_ConfigTable.setRedraw(true);
				m_ConfigTable.setSelection(selectedIdx+1);
				m_ConfigTable.setFocus();
			}
		}
	}
	
	/**
	 * Class called whenever the browse button is pressed
	 *
	 */
	private class BrowseListener extends SelectionAdapter
	{
		public void widgetSelected(SelectionEvent e)
		{
			ContainerSelectionDialog dialog = new ContainerSelectionDialog(
					getShell(), getProject(), false, "Select a working folder");
			dialog.setValidator(new ISelectionValidator()
			{
				public String isValid(Object selection)
				{
					if (selection.toString().indexOf(getProjectPath()) == 0)
						return null;
					else
						return "Cannot select a folder in other projects";
				}
				
			});
			if (dialog.open() == ContainerSelectionDialog.OK)
			{
				Object[] result = dialog.getResult();
				if (result.length == 1)
				{
					String path = ((Path)result[0]).toString();
					path = path.substring(getProjectPath().length() + 1);
					m_WorkFolder.setText(path);
				}
			}
		}
	}
	
	/**
	 * Used to listen for selection changes
	 */
	private class ConfigTableSelectionListener extends SelectionAdapter{
		public void widgetSelected(SelectionEvent e)
		{
			if (e.item instanceof TableItem) {
				TableItem item = (TableItem) e.item;
				BuildConfig command=(BuildConfig)item.getData();
				
				m_Name.setText(command.getName());
				m_WorkFolder.setText(command.getWorkFolder());
				m_CleanCommand.setText(command.getCleanCommand());
				m_BuildCommand.setText(command.getCommand());
				int idx=0;
				m_ErrorParser.select(-1);
				for(String errorParser: m_ErrorParser.getItems()){
					if(errorParser.equals(command.getParser())){
						m_ErrorParser.select(idx);
					}
					idx++;
				}
			}
		}
	}
	
	/**
	 * Used to listen to focus changes in order to save the users input
	 *
	 */
	private class FieldsFocusListener extends FocusAdapter{
		public void	focusLost(FocusEvent e){
			saveFields();
		}
	}
	
	/**
	 * Called when the user types something in the name list
	 *
	 */
	private class NameInputListener extends KeyAdapter{
		public void keyReleased(KeyEvent e){
			TableItem[] selectedItems=m_ConfigTable.getSelection();
			
			//there should be only one selected item
			if(selectedItems.length > 0){
				//update the item name
				selectedItems[0].setText(CONFIG_NAME_COL_IDX,m_Name.getText());
			}
		}
	}
}


	



