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

import java.util.Vector;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.editor.HdlEditor;
import net.sourceforge.veditor.parser.OutlineElement;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
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
			Point size= new Point(200,200);
			m_ProposalShell.setSize(size);
			m_ProposalTable.setSize(size);
			m_ProposalTable.addFocusListener(new focusListener());
			m_ProposalTable.addSelectionListener(new selectionListener());		
		}
		m_ProposalTable.setItemCount(definitionList.size());		
		TableItem[] tableItem=m_ProposalTable.getItems();
		for(int i=0;i<tableItem.length;i++){
			tableItem[i].setText(definitionList.get(i).getName());
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

		Vector<OutlineElement> definitionList=
			doc.getDefinitionList(selectionText,selectionRange.x);
		

		//go to the definition
		if(definitionList.size() == 1){
			editor.showElement(definitionList.get(0));
		}
		else if(definitionList.size() > 1){
			for(int i = 0; i < definitionList.size(); i++)
			{
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



