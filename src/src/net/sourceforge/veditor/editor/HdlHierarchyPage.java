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

import java.util.Vector;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.parser.OutlineDatabase;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.OutlineDatabase.OutlineDatabaseEvent;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogInstanceElement;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.Page;

public class HdlHierarchyPage extends Page implements ISelectionChangedListener,
		IDoubleClickListener
{
    private TreeViewer m_TreeViewer;
    private HdlEditor  m_Editor;
    private Clipboard  m_Clipboard;
    private ISelection m_Selection;
    private Action     m_RefreshAction;
    private Action     m_CopyTextAction;
    private Action     m_CopyHierarchyAction;
    private Action     m_GotoDefinition;
    private Action     m_CollapseAllAction;
    private Action     m_RescanAllAction;
    private Action     m_EnableSortAction;
    private boolean enableSort;

    private final static String REFRESH_ACTION_IMAGE="$nl$/icons/refresh.gif";
    private final static String GOTO_DEF_ACTION_IMAGE="$nl$/icons/goto_def.gif";
	private static final String ENABLE_SORT_ACTION_IMAGE="$nl$/icons/sort.gif";
    
	public HdlHierarchyPage(HdlEditor editor) {
		super();
		this.m_Editor = editor;
		m_Clipboard = new Clipboard(Display.getCurrent());
		m_Selection = null;
		m_TreeViewer = null;
		m_CollapseAllAction = null;
		m_RescanAllAction = null;
		m_EnableSortAction = null;
		enableSort = VerilogPlugin.getPreferenceBoolean("Outline.Sort");
	}
	
	public void dispose() {
		super.dispose();
		m_Selection = null;
		m_TreeViewer = null;
	}
	
	public boolean isDisposed(){
		return m_TreeViewer==null;
	}
	
	
	protected class databaseListner extends OutlineDatabaseEvent{

		@Override
		public void handel() {
			   update();        
		}
		
	}
	
	public void createControl(Composite parent)
	{
		m_TreeViewer=new TreeViewer(parent);
		createActions();		
		createMenu();
		createToolbar();
		createContextMenu(m_TreeViewer.getTree());

		m_TreeViewer.setContentProvider(m_Editor.getHirarchyProvider());
		m_TreeViewer.setLabelProvider(m_Editor.getOutlineLabelProvider());
		m_TreeViewer.addSelectionChangedListener(this);
		m_TreeViewer.addDoubleClickListener(this);
		if (enableSort)
			m_TreeViewer.setSorter(new ViewerSorter());
		IDocument doc = m_Editor.getDocument();
		if (doc != null)
		{
			m_TreeViewer.setInput(doc);
			m_TreeViewer.collapseAll();
			//register the update function
			if (doc instanceof HdlDocument) {
				HdlDocument hdlDoc = (HdlDocument) doc;				
				hdlDoc.getOutlineDatabase().addChangeListner(new databaseListner());
			}
		}			
	}
	/**
	 * creates the actions
	 */
	private void createActions() {
		m_CopyTextAction = new CopyTextAction();
		m_CopyHierarchyAction = new CopyHierarchyAction();
		m_RefreshAction = new RefreshAction();
		m_GotoDefinition = new GotoDefinitionAction();
		m_CollapseAllAction = new CollapseAllAction();
		m_RescanAllAction = new RescanAllAction();
		m_EnableSortAction = new EnableSortAction();
	}

	/** 
	 *  creates a menu for this view
	 */
	private void createMenu(){
		
	}
	/**
	 * Creates a toolbar for this view
	 */
	private void createToolbar(){
		IToolBarManager mgr = getSite().getActionBars().getToolBarManager();
		mgr.add(m_EnableSortAction);
        mgr.add(m_RefreshAction);
        mgr.add(m_CollapseAllAction);
        mgr.add(m_RescanAllAction);
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
				if(m_Selection != null){
					menu.add(m_CopyTextAction);
					menu.add(m_CopyHierarchyAction);
					menu.add(m_GotoDefinition);
				}
				menu.add(m_RefreshAction);
				menu.add(m_CollapseAllAction);				
			}
		});
		Menu menu = menuManager.createContextMenu(control);		
		control.setMenu(menu);			
	}

	public Control getControl()
	{
		if (m_TreeViewer == null)
			return null;
		return m_TreeViewer.getControl();
	}

	public void setFocus()
	{
		m_TreeViewer.getControl().setFocus();
	}

	public void setInput(Object input)
	{
//		update();
	}
	
	
	/**
	 * Searches through the tree item recursively and looks the specified element
	 * @param element
	 * @param item
	 * @return
	 */
	protected TreeItem findTreeItem(TreeItem item,OutlineElement element){
		if (item.getData() instanceof OutlineElement) {
			OutlineElement e = (OutlineElement) item.getData();
			if(e.equals(element)){
				return item;
			}			
		}else
		{
			return null;
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
	 * scans the tree and selects the item
	 * @param element
	 * @return true if item was found and is select, false otherwise
	 */
	private boolean selectElement(OutlineElement element){
		TreeItem target=null;		
		if(element == null){			
			return false;
		}

		TreeItem[] treeItems=m_TreeViewer.getTree().getItems();
		Vector<TreeItem> itemsVector=new Vector<TreeItem>();
		for(TreeItem item: treeItems){
			target=findTreeItem(item, element);
			if(target!=null){
				itemsVector.add(target);							
			}
		}
		//since the hierarchy tree may only include some of the outline elements,
		//we may need seek backwards in the parent's history to find something in
		//the hierarchy list
		if(itemsVector.size()==0){
			return selectElement(element.getParent());
		}else{
			TreeItem[] items=itemsVector.toArray(new TreeItem[0]);
			RGB rgb=new RGB(255,255,10);
			Color bgColor=new Color(Display.getCurrent(), rgb);
	
			m_TreeViewer.collapseAll();
			//expand the node and its parents
			for(TreeItem item:items){				
				TreeItem tempTreeItem=item;
				item.setBackground(bgColor);
				do{
					tempTreeItem.setExpanded(true);
					
					tempTreeItem=tempTreeItem.getParentItem();					
				}while(tempTreeItem != null);				
			}	
		}
		
		return true;
		
	}
	/**
	 * Searches for the given element and if found, makes it visible
	 * @param element
	
	 */
	public void showElement(OutlineElement element){	
		
		m_TreeViewer.getControl().setRedraw(false);
		update();
		Object[] expandedElements=m_TreeViewer.getExpandedElements();
		//force all the elements to be scanned in
		m_TreeViewer.expandAll();
		m_TreeViewer.setExpandedElements(expandedElements);		
		if(selectElement(element) == false){
			Display.getCurrent().beep();
		}
		m_TreeViewer.getControl().setRedraw(true);
		
		
	}
	
	public void update() {
		if (m_TreeViewer != null) {
			if (enableSort)
				m_TreeViewer.setSorter(new ViewerSorter());
			else
				m_TreeViewer.setSorter(null);
			Control control = m_TreeViewer.getControl();
			IDocument doc = m_Editor.getDocument();
			if (control != null && !control.isDisposed() && doc != null) {
				Object expanded[] = m_TreeViewer.getExpandedElements();
				control.setRedraw(false);
				m_TreeViewer.setInput(doc);
				if (expanded.length > 0) {
					m_TreeViewer.setExpandedElements(expanded);
				} else {
					m_TreeViewer.collapseAll();
				}
				control.setRedraw(true);
			}
		}
	}

	public void selectionChanged(SelectionChangedEvent event)
	{
		m_Selection = event.getSelection();
	}

	public void doubleClick(DoubleClickEvent event)
	{
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection elements = (IStructuredSelection)selection;
			if (elements.size() == 1)
			{
				Object element = elements.getFirstElement();
				if (element instanceof OutlineElement) {
					OutlineElement outlineElement = (OutlineElement) element;
					m_Editor.showElement(outlineElement);
				}
			}
		}
	}

	/**
	 * set text to clipboard<p>
	 * it is called from the Actions
	 */
	private void setClipboard(StringBuffer text)
	{
		TextTransfer plainTextTransfer = TextTransfer.getInstance();
		m_Clipboard.setContents(new String[] { text.toString() },
				new Transfer[] { plainTextTransfer });
	}
	
	/**
	 * Class used to copy the items text to the clip board
	 */
	private class CopyTextAction extends Action
	{
		public CopyTextAction()
		{
			super();
			setText("Copy Text");
		}
		public void run()
		{
			StringBuffer text = new StringBuffer();

			if (m_Selection instanceof IStructuredSelection)
			{
				Object[] ary = ((IStructuredSelection)m_Selection).toArray();
				for (int i = 0; i < ary.length; i++)
				{
					text.append(ary[i].toString() + "\n");
				}
				setClipboard(text);
			}
		}
	}

	private class EnableSortAction extends Action {
		public EnableSortAction() {
			super();
			setText("Sort");
			setChecked(enableSort);
		}

		public void run() {
			enableSort = !enableSort;
			update();
		}

		public ImageDescriptor getImageDescriptor() {
			return VerilogPlugin.getPlugin().getImageDescriptor(
					ENABLE_SORT_ACTION_IMAGE);
		}

		public int getStyle() {
			return AS_CHECK_BOX;
		}

		public String getToolTipText() {
			return "Sort";
		}
	}

	/**
	 * Class used to perform the refresh action
	 */
	private class RefreshAction extends Action
	{
		public RefreshAction()
		{
			super();
			setText("Refresh Hierarchy");
		}
		public void run()
		{
			update();
		}
		/**
		 * Gets an image descriptor for this action
		 */
		public ImageDescriptor getImageDescriptor(){
			return VerilogPlugin.getPlugin().getImageDescriptor(REFRESH_ACTION_IMAGE);
		}
	}
	/**
	 * Class used to perform the refresh action
	 */
	private class GotoDefinitionAction extends Action
	{
		public GotoDefinitionAction()
		{
			super();
			setText("Goto Definition");
		}
		public void run() {
			if (m_Selection instanceof IStructuredSelection) {
				IStructuredSelection elements = (IStructuredSelection) m_Selection;
				if (elements.size() == 1) {
					Object element = elements.getFirstElement();

					if (element instanceof VerilogInstanceElement) {
						VerilogInstanceElement instance = (VerilogInstanceElement) element;
						OutlineDatabase database = getOutlineDatabase();
						if (database != null) {
							OutlineElement module = database.findDefinition(instance);
							if (module != null)
								m_Editor.showElement(module);
						}

					} else if (element instanceof OutlineElement) {
						OutlineElement outlineElement = (OutlineElement) element;
						m_Editor.showElement(outlineElement);
					}
				}
			}
		}
		/**
		 * Gets an image descriptor for this action
		 */
		public ImageDescriptor getImageDescriptor(){
			return VerilogPlugin.getPlugin().getImageDescriptor(GOTO_DEF_ACTION_IMAGE);
		}
	}
	private class CollapseAllAction extends Action	
	{
		private static final String COLLAPSE_ALL_ACTION_IMAGE="$nl$/icons/collapse_all.gif";
		public CollapseAllAction()
		{
			super();
			setText("Collapse All");
		}
		public void run()
		{
			m_TreeViewer.collapseAll();
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
	private class RescanAllAction extends Action	
	{
		private static final String COLLAPSE_ALL_ACTION_IMAGE="$nl$/icons/rescan.gif";
		public RescanAllAction()
		{
			super();
			setText("Rescan All");
		}
		public void run()
		{
			OutlineDatabase database = getOutlineDatabase();
			if(database != null) 
				database.scanProject();
		}
		public ImageDescriptor getImageDescriptor(){
			return VerilogPlugin.getPlugin().getImageDescriptor(COLLAPSE_ALL_ACTION_IMAGE);
		}
		public int getStyle(){
			return AS_PUSH_BUTTON;
		}
		public String getToolTipText(){
			return "Rescan all HDL files";
		}
	}
	
	/**
	 * utility function for getting OutlineDatabase
	 */
	private OutlineDatabase getOutlineDatabase() {
		IProject project = m_Editor.getHdlDocument().getProject();
		OutlineDatabase database = null;
		try {
			database = (OutlineDatabase) project
					.getSessionProperty(VerilogPlugin.getOutlineDatabaseId());
		} catch (CoreException e) {
			return null;
		}
		return database;
	}
	
	/**
	 * Class used to copy the hierarchy
	 */
	private class CopyHierarchyAction extends Action
	{
		public CopyHierarchyAction()
		{
			super();
			setText("Copy Hierarchy");
		}
		public void run()
		{
			StringBuffer text = new StringBuffer();

			if (m_Selection instanceof IStructuredSelection)
			{
				Object[] ary = ((IStructuredSelection)m_Selection).toArray();
				addText(text, 0, ary);

				setClipboard(text);
			}
		}

		/**
		 * add text with indent of hierarchy
		 * @param text	destination string
		 * @param level	hierarchical level
		 * @param ary	instance array
		 */
		private void addText(StringBuffer text, int level, Object[] ary)
		{
			if (ary == null)
				return;
			for (int i = 0; i < ary.length; i++)
			{
				for (int j = 0; j < level; j++)
					text.append("    ");
				text.append(ary[i].toString() + "\n");

			}
		}
	}	

}


