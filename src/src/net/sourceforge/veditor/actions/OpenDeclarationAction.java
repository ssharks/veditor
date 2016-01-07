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
package net.sourceforge.veditor.actions;

import java.awt.List;
import java.util.Vector;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.document.VhdlDocument;
import net.sourceforge.veditor.editor.HdlEditor;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.OutlineContainer;
import net.sourceforge.veditor.parser.OutlineDatabase;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.ArchitectureElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.PackageDeclElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.VhdlOutlineElement;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorSite;


/**
 * find module declaration from project tree<p>
 * file name and module name must be same
 */
public class OpenDeclarationAction extends AbstractAction
{
	Shell m_ProposalShell;
	Table m_ProposalTable;
	private static final String GOTO_DEF_ACTION_IMAGE="$nl$/icons/goto_def.gif";
	
	public OpenDeclarationAction()
	{
		super("OpenDeclaration");
		m_ProposalShell=null;
	}
	public void run()
	{
//		StyledText widget = getViewer().getTextWidget();
//
//		String modName = widget.getSelectionText();
//		if (modName.equals(""))
//		{
//			beep();
//			return;
//		}
//		getEditor().openPage(modName);
		
		gotoDefinition();
	}
	
	public ImageDescriptor getImageDescriptor(){
		return VerilogPlugin.getPlugin().getImageDescriptor(GOTO_DEF_ACTION_IMAGE);
	}
	
	private class focusListener implements FocusListener{

		public void focusGained(FocusEvent e) {
			
		}

		public void focusLost(FocusEvent e) {
			m_ProposalShell.setVisible(false);
		}
		
	}
	
	private class selectionListener implements SelectionListener{

		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void widgetSelected(SelectionEvent e) {
			
			if (e.item instanceof TableItem) {
				TableItem tableItem = (TableItem) e.item;
				if (tableItem.getData() instanceof OutlineElement) {
					OutlineElement element = (OutlineElement) tableItem.getData();
					getEditor().showElement(element);
				}
			}
			m_ProposalShell.setVisible(false);
		}
		
	}
	
	private void showPopUp(Vector<OutlineElement> definitionList,HdlEditor editor,Point position){	
		IEditorSite control=editor.getEditorSite();
		
		if(m_ProposalShell == null){
			m_ProposalShell= new Shell(control.getShell(), SWT.ON_TOP);
			m_ProposalShell.setFont(JFaceResources.getDefaultFont());
			GridLayout layout= new GridLayout();
			layout.marginWidth= 0;
			layout.marginHeight= 0;
			layout.verticalSpacing= 1;		
			m_ProposalShell.setLayout(layout);		
			m_ProposalTable= new Table(m_ProposalShell, SWT.H_SCROLL | SWT.V_SCROLL);
			m_ProposalTable.setLocation(0, 0);
			GridData data= new GridData(GridData.FILL_BOTH);
			m_ProposalTable.setLayoutData(data);
			Point size= new Point(400,200);
			m_ProposalShell.setSize(size);
			m_ProposalTable.setSize(size);
			m_ProposalTable.addFocusListener(new focusListener());
			m_ProposalTable.addSelectionListener(new selectionListener());		
		}
		m_ProposalTable.setItemCount(definitionList.size());		
		TableItem[] tableItem=m_ProposalTable.getItems();
		for(int i=0;i<tableItem.length;i++){
			tableItem[i].setText(definitionList.get(i).getFile().getName() + " : " +
				definitionList.get(i).getLongName());
			String imageName=definitionList.get(i).GetImageName();
			if(imageName!=null){
				tableItem[i].setImage(VerilogPlugin.getPlugin().getImage(imageName));
			}
			tableItem[i].setData(definitionList.get(i));
		}		
				
		m_ProposalShell.setLocation(position);
		m_ProposalShell.setVisible(true);
		m_ProposalTable.setFocus();		
	
	}
	
	/**
	 * Attempts to find the definition for the highlighted word
	 */
	public void gotoDefinition(){
		HdlEditor   editor= getEditor();
		HdlDocument doc   = editor.getHdlDocument();
		StyledText widget = getViewer().getTextWidget();
		String selectionText = widget.getSelectionText();
		Point  selectionRange = widget.getSelection();
		Point  selectionPos   = widget.getLocationAtOffset(selectionRange.y);

		Vector<OutlineElement> definitionList = new Vector<OutlineElement>(
			doc.getDefinitionList(selectionText,selectionRange.x));

		//go to the definition
		if(definitionList.size() > 0) {
			Vector<OutlineElement> directPackageHits = new Vector<OutlineElement>();
			
			// check if there is a package name in front of this keyword we are looking for
			for (int i = 0; i < definitionList.size(); i++) {
				OutlineElement parent = definitionList.get(i).getParent();
				if ((parent != null) && (parent instanceof PackageDeclElement)) {
					// look if the name of the package is there
					String parentName = parent.getName() + ".";
					int offset = selectionRange.x - parent.getName().length();
					// get the length of the name leading to the selection
					String leadingString = "";
					try {
						leadingString = doc.get(offset, parent.getName().length());
					} catch (BadLocationException e) {
					}
					
					// compare with the package name
					if (leadingString.equalsIgnoreCase(parentName)) {
						directPackageHits.add(definitionList.get(i));
					}
				}
			}
			
			// when one or more elements are found, remove all the package elements
			// from the list and replace them by the direct addressed ones
			if (directPackageHits.size() > 0) {
				for (int i = definitionList.size(); i >= 0; i--) {
					OutlineElement parent = definitionList.get(i).getParent();
					if ((parent != null) && (parent instanceof PackageDeclElement)) {
						definitionList.remove(i);
					}
				}
				definitionList.addAll(directPackageHits);
			}
		
		} else {
			// not found in this file, search in packages of other files
			OutlineDatabase database = doc.getOutlineDatabase();
			// try the get the outline container of this document, start wih ina empty one
			OutlineContainer docContainer = new OutlineContainer();
			try {
				docContainer = doc.getOutlineContainer(false);
			} catch (HdlParserException e) {
			}
			if (database != null) {
				OutlineElement[] elements = database.findTopLevelElements("");
				Vector<OutlineElement> subPackageHits = new Vector<OutlineElement>();
				for (int i = 0; i < elements.length; i++) {
					// jump to architecture
					if(elements[i] instanceof ArchitectureElement ){
						ArchitectureElement architureElement =(ArchitectureElement)elements[i];
						if(architureElement.GetEntityName().equalsIgnoreCase(selectionText)){
							definitionList.add(architureElement);
						}
					}
					
					if(elements[i] instanceof PackageDeclElement ){
						PackageDeclElement packageDeclElement = (PackageDeclElement)elements[i];
						if (packageDeclElement.getName().equalsIgnoreCase(selectionText)) {
							definitionList.add(packageDeclElement);
						}
					}
				}
			}
			definitionList.addAll(
				doc.getPackageElementByName(selectionText, false, selectionRange.x));
		}
		
		if(definitionList.size() == 1){
			// when only one element is found, jump to it now
			editor.showElement(definitionList.get(0));
		}
		else if(definitionList.size() > 1){
			for(int i = 0; i < definitionList.size(); i++)
			{
				// when a module is found, directly jump to it
				OutlineElement element = definitionList.get(i);
				if (element.getType().equals("module#"))
				{
					editor.showElement(element);
					return;
				}
			}
			
			// if module is not found, show popup
			showPopUp(definitionList, editor, selectionPos);
		}
	}
}



