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

package net.sourceforge.veditor.editor;


import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogFunctionElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogInstanceElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogModuleElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogParameterElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogPortElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogRegElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogSignalElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogTaskElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogWireElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.AliasElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.ArchitectureElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.ComponentDeclElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.ComponentInstElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.ConstantElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.EntityDeclElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.EntityInstElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.GenericElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.PackageBodyElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.PackageDeclElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.ProcedureElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.ProcessElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.VhdlPortElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.VhdlSignalElement;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class HdlContentOutlinePage extends ContentOutlinePage
{
	private HdlEditor editor;
	private static final String FILTER_SIGNAL_ACTION_IMAGE="$nl$/icons/filter_signal.gif";
	private boolean m_bFilterSignals;

	public HdlContentOutlinePage(HdlEditor editor)
	{
		super();
		this.editor = editor;
		m_bFilterSignals=false;
	}

	public void createControl(Composite parent)
	{
		super.createControl(parent);

		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new HdlContentOutlineProvider());
		viewer.setLabelProvider(editor.getOutlineLabelProvider());
		viewer.addSelectionChangedListener(this);
		viewer.addFilter(new signalFilter());
		viewer.setSorter(new sorter());
		createToolbar();
		createContextMenu(viewer.getTree());

		IDocument doc = editor.getDocument();
		if (doc != null){
			viewer.setInput(doc);			
		}		
		
	}

	
	private void createToolbar(){
		IToolBarManager mgr = getSite().getActionBars().getToolBarManager();
        mgr.add(new signalFilterAction());
        mgr.add(new collapseAllAction());
	}
	
	/**
	 * Creates a context menu for this view
	 * @param control
	 */
	private void createContextMenu(Control control)
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menu)
			{			
				menu.add(new collapseAllAction());
			}
		});
		Menu menu = menuManager.createContextMenu(control);		
		control.setMenu(menu);			
	}
	
	public void selectionChanged(SelectionChangedEvent event)
	{
		super.selectionChanged(event);

		ISelection selection = event.getSelection();

		if (!selection.isEmpty())
		{
			OutlineElement outlineElement =
				(OutlineElement) ((IStructuredSelection)selection).getFirstElement();			
					
			editor.showElement(outlineElement);						
		}
	}
	
	/**
	 * Searches throught the tree item recursively and looks the specified element
	 * @paaram element
	 * @param item
	 * @return
	 */
	protected TreeItem findTreeItem(TreeItem item,OutlineElement element){
		if (item.getData() instanceof OutlineElement) {
			OutlineElement e = (OutlineElement) item.getData();
			if(e.equals(element)){
				return item;
			}			
		}
		//look through the children
		for(TreeItem child:item.getItems()){
			TreeItem temp=findTreeItem(child,element);
			if(temp!=null){
				return temp;
			}
		}
		
		return null;
	}
	
	/**
	 * Searches for the given element and if found, makes it visible
	 * @param element
	 */
	public void showElement(OutlineElement element){
		if(element == null)
			return;
		TreeViewer viewer = getTreeViewer();		
		viewer.reveal(element);				
		TreeItem[] treeItems=viewer.getTree().getItems();
		
		viewer.getControl().setRedraw(false);
		Object[] expandedElements=viewer.getExpandedElements();
		//force all the elements to be scanned in
		viewer.expandAll();		
		viewer.setExpandedElements(expandedElements);
		viewer.getControl().setRedraw(true);
		
		for(TreeItem item: treeItems){
			TreeItem target=findTreeItem(item, element);
			if(target!=null){
				viewer.getTree().setSelection(target);
				viewer.reveal(element);
				break;
			}
		}				
		
	}
	
	public void setInput(Object input)
	{
		//update();
	}

	public void update()
	{
		TreeViewer viewer = getTreeViewer();

		if (viewer != null)
		{
			Control control = viewer.getControl();
			if (control != null && !control.isDisposed())
			{				
				Object expanded[]=viewer.getExpandedElements();
				control.setRedraw(false);				 
				viewer.setInput(editor.getDocument());					
				if(expanded.length >0){
					viewer.setExpandedElements(expanded);
				}
				else{
					viewer.collapseAll();
				}
				control.setRedraw(true);
			}
		}
	}
	
	class signalFilter extends ViewerFilter{

		public boolean select(Viewer viewer, Object parentElement,
				Object element) {

			if(m_bFilterSignals){
				if (element instanceof VhdlSignalElement){
					return false;
				}
				if (element instanceof VerilogSignalElement){
					return false;
				}
			}
			return true;
		}
		
	}
	
	/**
	 * Class used to sort the outline elements
	 *
	 */
	class sorter extends ViewerSorter{
		/**
		 * Called to determine whether an object is sortable or not
		 */
		public boolean isSorterProperty(Object element,
                String property){
			return super.isSorterProperty(element,property);
		}
		public int compare(Viewer viewer, Object e1, Object e2) {
			return super.compare(viewer,e1,e2);
		}
		public int category(Object element) {
			//vhdl catagories			
			if (element instanceof PackageDeclElement) return 10;
			if (element instanceof PackageBodyElement) return 15;
			if (element instanceof EntityDeclElement) return 20;
			if (element instanceof ComponentDeclElement) return 20;
			if (element instanceof EntityDeclElement) return 20;
			if (element instanceof ArchitectureElement) return 30;
			if (element instanceof GenericElement) return 40;
			if (element instanceof VhdlPortElement) return 45;
			if (element instanceof ConstantElement) return 50;
			if (element instanceof AliasElement) return 60;
			if (element instanceof VhdlSignalElement) return 70;
			if (element instanceof EntityInstElement) return 80;
			if (element instanceof ComponentInstElement) return 80;
			if (element instanceof VerilogFunctionElement) return 90;
			if (element instanceof ProcedureElement) return 90;
			if (element instanceof ProcessElement) return 100;
			
			
			//verilog elements can go here			
			if (element instanceof VerilogModuleElement) return 10;
			if (element instanceof VerilogFunctionElement) return 14;
			if (element instanceof VerilogTaskElement) return 15;			
			if (element instanceof VerilogParameterElement) return 20;
			if (element instanceof VerilogPortElement) return 30;
			if (element instanceof VerilogRegElement) return 40;
			if (element instanceof VerilogWireElement) return 50;			
			if (element instanceof VerilogSignalElement) return 60;
			if (element instanceof VerilogInstanceElement) return 70;
			//default sort
			return 100000;
		}
	}
	
	private class signalFilterAction extends Action
	{
		public signalFilterAction()
		{
			super();
			setText("Filter Signal");
		}
		public void run()
		{
			m_bFilterSignals = !m_bFilterSignals;
			update();
		}
		public ImageDescriptor getImageDescriptor(){
			return VerilogPlugin.getPlugin().getImageDescriptor(FILTER_SIGNAL_ACTION_IMAGE);
		}
		public int getStyle(){
			return AS_CHECK_BOX;
		}
		public boolean GetChecked(){
			return m_bFilterSignals;
		}
		public String getToolTipText(){
			return "Filter signals";
		}
	}
	
	private class collapseAllAction extends Action	
	{
		private static final String COLLAPSE_ALL_ACTION_IMAGE="$nl$/icons/collapse_all.gif";
		public collapseAllAction()
		{
			super();
			setText("Collapse All");
		}
		public void run()
		{
			TreeViewer viewer = getTreeViewer();
			viewer.collapseAll();
		}
		public ImageDescriptor getImageDescriptor(){
			return VerilogPlugin.getPlugin().getImageDescriptor(COLLAPSE_ALL_ACTION_IMAGE);
		}
		public int getStyle(){
			return AS_PUSH_BUTTON;
		}
		public String getToolTipText(){
			return "Collapse all";
		}
	}
}
