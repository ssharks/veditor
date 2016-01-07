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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.actions.ClearErrorMarkersAction;
import net.sourceforge.veditor.actions.CollapseAll;
import net.sourceforge.veditor.actions.CommentAction;
import net.sourceforge.veditor.actions.CompileAction;
import net.sourceforge.veditor.actions.ExpandAll;
import net.sourceforge.veditor.actions.FormatAction;
import net.sourceforge.veditor.actions.GotoMatchingBracketAction;
import net.sourceforge.veditor.actions.HdlShowInNavigatorAction;
import net.sourceforge.veditor.actions.OpenDeclarationAction;
import net.sourceforge.veditor.actions.ShowInHierarchy;
import net.sourceforge.veditor.actions.ShowInOutline;
import net.sourceforge.veditor.actions.SynthesizeAction;
import net.sourceforge.veditor.actions.UnCommentAction;
import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.OutlineContainer;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.OutlineContainer.Collapsible;
import net.sourceforge.veditor.preference.PreferenceStrings;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;


/**
 *  main class
 */
abstract public class HdlEditor extends TextEditor
{
	private ColorManager colorManager;
	private HdlContentOutlinePage outlinePage;
	protected IBaseLabelProvider OutlineLabelProvider;
	protected ITreeContentProvider TreeContentProvider;
	protected static HdlEditor current;
	protected ProjectionSupport m_ProjectionSupport;	
	protected HashMap<Collapsible,ProjectionAnnotation> m_CollapsibleElements;
	protected boolean m_bInitialShowing;
	
	public static HdlEditor current()
	{
		return current;
	}
	
	public HdlEditor()
	{
		super();

		setCurrent();
		colorManager = new ColorManager();	
		HdlTextAttribute.init();	
		m_bInitialShowing=true;
		m_CollapsibleElements=new HashMap<Collapsible,ProjectionAnnotation>();
		
		
		
	}	
	
	/**
	 * Makes this instance of the editor the current one
	 * @return the old editor
	 */
	public HdlEditor setCurrent(){
		HdlEditor old=current;
		current = this;
		return old;
	}
		
	public void updatePartControl(IEditorInput input)
	{
		super.updatePartControl(input);
		setInputPages(input);
	}

	protected void initializeKeyBindingScopes()
	{
		setKeyBindingScopes(new String[] { "net.sourceforge.veditor.scope" });
	}

	public IDocument getDocument()
	{
		IEditorInput editorInput=getEditorInput();
		IDocumentProvider provider=getDocumentProvider();
		
		if(provider != null && editorInput!=null){
			return provider.getDocument(editorInput);
		}
		
		return null;
		
	}

	protected void createActions()
	{
		super.createActions();

		// add content assist action
		IAction action;
		action =
			new TextOperationAction(
				EditorMessages.getResourceBundle(),
				"ContentAssistProposal.",
				this,
				ISourceViewer.CONTENTASSIST_PROPOSALS);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action);

		// add special actions
		setAction("GotoMatchingBracket", new GotoMatchingBracketAction());
		setAction("OpenDeclaration", new OpenDeclarationAction());
		setAction("Format", new FormatAction());
		setAction("Compile", new CompileAction());
		setAction("Synthesize", new SynthesizeAction());
		setAction("Comment", new CommentAction());
		setAction("Uncomment", new UnCommentAction());
		setAction("CollapseAll",new CollapseAll());
		setAction("ExpandAll",new ExpandAll());
		setAction("ShowInHierarchy",new ShowInHierarchy());
		setAction("ShowInOutline",new ShowInOutline());
		setAction(HdlShowInNavigatorAction.ID,new HdlShowInNavigatorAction());
		setAction(ClearErrorMarkersAction.ID,new ClearErrorMarkersAction());
		
	}

//	for content assist?
//	public void editorContextMenuAboutToShow(MenuManager menu)
//	{
//		super.editorContextMenuAboutToShow(menu);
//		addAction(menu, "ContentAssistProposal");
//		addAction(menu, "ContentAssistTip");
//	}

	public void dispose()
	{
		colorManager.dispose();
		setInputPages(null);
		storeCollapsibleStates();
		super.dispose();
	}

	public void doRevertToSaved()
	{
		super.doRevertToSaved();
		updatePages();
	}

	public void doSave(IProgressMonitor monitor)
	{
		super.doSave(monitor);
		updatePages();
	}

	public void doSaveAs()
	{
		super.doSaveAs();
		updatePages();
	}
	
	public void expandAll(){
		getAnnotation().expandAll(0, getHdlDocument().getLength());
	}
	
	public void collapseAll(){
		getAnnotation().collapseAll(0, getHdlDocument().getLength());
	}
	/**
	 * update outline and module hierarchy page
	 */
	private void updatePages()
	{
		checkSyntax();
		if (outlinePage != null)
			outlinePage.update();
		if (getHierarchyPage() != null)
			getHierarchyPage().update();
	}
	
	
	protected Point getCursorLocation(){
		String[] cursorStr=getCursorPosition().split(":");
		int line=0,col=0;
		if(cursorStr.length > 1){
			line=Integer.parseInt(cursorStr[0].trim());
			col=Integer.parseInt(cursorStr[1].trim());
		}
		
		return new Point(line,col);
	}
	
	
	/**
	 * shows the current object in the hierarchy
	 */
	public void showInHierarchy(){
		Point cursor=getCursorLocation();		

		OutlineContainer container;
		try {
			container = getHdlDocument().getOutlineContainer();
			if(container!=null){
				OutlineElement element=container.getLineContext(cursor.x, cursor.y);
				if ( element!=null  && getHierarchyPage() != null){
						getHierarchyPage().showElement(element);
				}
			}

		} catch (HdlParserException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Shows the current line in the hierarchy view
	 */
	public void showInOutline(){
		Point cursor=getCursorLocation();		

		OutlineContainer container;
		try {
			container = getHdlDocument().getOutlineContainer();
			if(container!=null){
				OutlineElement element=container.getLineContext(cursor.x, cursor.y);
				if (element!=null && outlinePage != null){
						outlinePage.showElement(element);
				}
			
			}	
		} catch (HdlParserException e) {		
			e.printStackTrace();
		}			
	}
	
	/**
	 * returns true if this editor has a hierarchy page
	 * @return
	 */
	public boolean hasHierarchy(){
		return (getHierarchyPage()!=null);
	}
	
	/**
	 * returns true if this editor has an outline page
	 * @return
	 */
	public boolean hasOutline(){
		return outlinePage!=null;
	}
	/**
	 * Called every time a the cursor position may change
	 */
	protected void handleCursorPositionChanged() {
		super.handleCursorPositionChanged();		
	}
	
	private HdlHierarchyPage getHierarchyPage(){
		HdlHierarchyPage page=null;
		
		HdlDocument doc=getHdlDocument();
		
		if(doc != null){
			IProject project=doc.getProject();
			if(project!=null){
				try {			
					page=(HdlHierarchyPage)project.getSessionProperty(VerilogPlugin.getHierarchyId());
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		
		return page;
	}
	private void setInputPages(IEditorInput input)
	{			
	    if (outlinePage != null)
			outlinePage.setInput(input);
		if (getHierarchyPage() != null)
			getHierarchyPage().setInput(input);
	}
	
	private void checkSyntax()
	{
		HdlDocument doc = getHdlDocument();	
		OutlineContainer outlineContainer;
		
		if(doc == null){
			return;
		}
		// check for non-workspace file
		IFile file = doc.getFile();
		if (file == null)
			return;
		
		try
		{
			outlineContainer=doc.getOutlineContainer();
			//update the folding structure
			updateFoldingStructure(outlineContainer.getCollapsibleRegions());
		}
		catch (HdlParserException e)
		{			
			
		}		
	}
	
	public void setFocus(){
		super.setFocus();
		setCurrent();
		if(m_bInitialShowing){
			checkSyntax();
			m_bInitialShowing=false;
			restoreCollapsibleStates();
		}
	}
	
	/**
	 * update editor view for problem marker.
	 */
	public void update()
	{
		//TODO Had to remove the following because it caused the document
		// to become disconnected from the Annotation Model. This funtion is only
		// called from the compile action to update the markers? I'm not really sure
		// it is even needed.
		
//		try
//		{
//			StyledText widget = getViewer().getTextWidget();
//			int caret = widget.getCaretOffset();
//			int top = widget.getTopIndex();
//			
//			super.doSetInput(getEditorInput());
//			
//			// widget might change in doSetInput
//			widget = getViewer().getTextWidget();
//			widget.setSelection(caret);
//			widget.setTopIndex(top);
//		}
//		catch (CoreException e)
//		{
//		}
	}
	
	public void doSetInput(IEditorInput input) throws CoreException
	{
		super.doSetInput(input);
		setInputPages(input);
	}

	public Object getAdapter(Class required)
	{
		HdlHierarchyPage page=null;
		// System.out.println("HdlEditor.getAdapter : " + required);
		if (IContentOutlinePage.class.equals(required))
		{
			if (outlinePage == null)
			{
				outlinePage = new HdlContentOutlinePage(this);
				if (getEditorInput() != null)
					outlinePage.setInput(getEditorInput());
			}
			return outlinePage;
		}
		else if (HdlHierarchyPage.class.equals(required))
		{
			if (getHierarchyPage() == null)
			{
				if (getHdlDocument().getProject() != null)
				{
					page = new HdlHierarchyPage(this);
					if (getEditorInput() != null)
						page.setInput(getEditorInput());
				}
			}
			return page;
		}
		return super.getAdapter(required);
	}

	public HdlDocument getHdlDocument()
	{
		IDocument doc = getDocument();
		if (doc instanceof HdlDocument)
			return (HdlDocument)doc;
		else
			return null;
	}

	protected void initializeEditor()
	{
		super.initializeEditor();
		// install a change lister that gets called when the user
		// changes one of this plugin's preferences
		IEclipsePreferences.IPreferenceChangeListener fPropertyChangeListener;	    
		fPropertyChangeListener= new IEclipsePreferences.IPreferenceChangeListener() {	    

            @Override
            public void preferenceChange(PreferenceChangeEvent arg0) {
                if(arg0.getKey().equals(PreferenceStrings.INDENT_TYPE)){ 
                   if(arg0.getNewValue().equals(PreferenceStrings.INDENT_TAB)){                  
                       uninstallTabsToSpacesConverter();
                   }
                   else{                      
                       installTabsToSpacesConverter();
                   }                       
                 }                  
                if(arg0.getKey().equals(PreferenceStrings.INDENT_SIZE)){
                    uninstallTabsToSpacesConverter();
                    installTabsToSpacesConverter();
                }
            }
	    };
	    
	    IEclipsePreferences peference = new InstanceScope().getNode(VerilogPlugin.ID);	    
	    peference.addPreferenceChangeListener(fPropertyChangeListener);
	}

	protected void editorContextMenuAboutToShow(IMenuManager menu)
	{
		MenuManager showIn=null;
		super.editorContextMenuAboutToShow(menu);
		menu.add(new Separator());
		menu.add(getAction("Format"));
		menu.add(getAction("OpenDeclaration"));
		menu.add(getAction("CollapseAll"));
		menu.add(getAction("ExpandAll"));
		menu.add(getAction(ClearErrorMarkersAction.ID));
		
		IContributionItem[] contributionItems=menu.getItems();
		for(IContributionItem item: contributionItems){
			if (item instanceof MenuManager) {
				MenuManager menuManager = (MenuManager) item;
				if(menuManager.getMenuText().startsWith("Sho&w In")){
					showIn=menuManager;
					break;
				}
			}
		}
		if(showIn!=null){
			//FIXME Need to figure out how to properly add stuff to the "Show In"
			if(hasHierarchy()) 
				showIn.add(getAction("ShowInHierarchy"));
			if(hasOutline())
				showIn.add(getAction("ShowInOutline"));
			showIn.add(getAction(HdlShowInNavigatorAction.ID));
		}
		
	}

	public void beep()
	{
		Display.getCurrent().beep();
	}
	public ISourceViewer getViewer()
	{
		return getSourceViewer();
	}

		
	/**
	 * Opens the definitions of an outline element
	 * @param element
	 */
	public void showElement(OutlineElement element) {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		try {
			// Determine the editor descriptor for the given file (generally
			// VHDL or verilog)
			IEditorDescriptor editorDesc = IDE.getEditorDescriptor(element.getFile(), true);

			// Create the editor instance
			IEditorPart editorPart = page.openEditor(new FileEditorInput(element.getFile()),
					editorDesc.getId());			
			
			if (editorPart instanceof HdlEditor) {
				HdlEditor editor = (HdlEditor) editorPart;
				IDocument doc = editor.getDocument();
				
				//go to the line
				int line = element.getStartingLine() - 1;
				
				int start = doc.getLineOffset(line);
				int length = doc.getLineOffset(line + element.getLength()) - start;				
				editor.setHighlightRange(start, length, true);
				editor.getSourceViewer().revealRange(start, length);
				markInNavigationHistory();
			}
		} catch (PartInitException e) {
			System.out.println(e);			
		} catch (BadLocationException e) {
			System.out.println(e);
		}
	}

	/**
	 * @return Returns the colorManager.
	 */
	public ColorManager getColorManager()
	{
		return colorManager;
	}	
	
	public IBaseLabelProvider getOutlineLabelProvider(){
		return OutlineLabelProvider;
	}

	public ITreeContentProvider getHirarchyProvider() {
		return TreeContentProvider;
	}
	
	public void createPartControl(Composite parent)
	{
	    super.createPartControl(parent);
	    ProjectionViewer viewer =(ProjectionViewer)getSourceViewer();

	    m_ProjectionSupport = new ProjectionSupport(viewer,getAnnotationAccess(),getSharedColors());
	    m_ProjectionSupport.install();

	    //turn projection mode on
	    viewer.doOperation(ProjectionViewer.TOGGLE);
	    
	    if (viewer instanceof SourceViewer) {
			viewer.addPostSelectionChangedListener(new MarkSelectionOccurences(this));
		}
	}
	
	protected ISourceViewer createSourceViewer(Composite parent,
			IVerticalRuler ruler, int styles) {
		ISourceViewer viewer = new ProjectionViewer(parent, ruler,
				getOverviewRuler(), isOverviewRulerVisible(), styles);

		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		

	    
		return viewer;
	}
	
	protected Position getElementPosition(Collapsible collapsible){
		Position  results= new Position(0,0);
		int start,end;
					   
		   HdlDocument doc=getHdlDocument();			
						
			try {
				start=doc.getLineOffset(collapsible.startLine-1);
				try{
					end=doc.getLineOffset(collapsible.endLine);
				}
				catch(BadLocationException e){
					//second chance
					//If there is not and end of line at the end of the
					//last line in a file, getLineOffset fails
					//in that case, get the last document character
					end=doc.getLength();
				}
				
				results.setOffset(start);
				results.setLength(end-start);			    
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return results;
	}
	/**
	 * Saves the state of collapsed items
	 * @param file
	 */
	protected void storeCollapsibleStates(){
		String stateString;		
		StringBuffer buffer = new StringBuffer();
		HdlDocument doc = getHdlDocument();

		if (doc == null)
		    return;
		//check for non-workspace file
		IFile file = doc.getFile();
		if (file == null)
			return;

		for(Collapsible collapsible:m_CollapsibleElements.keySet().toArray(new Collapsible[0])){
			if(m_CollapsibleElements.get(collapsible).isCollapsed()){
				buffer.append(collapsible.hashCode());
				buffer.append('#');
			}
		}
		stateString=buffer.toString();
		try {
			file.setPersistentProperty(VerilogPlugin.getCollapsibleId(),
					stateString);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/**
	 * Restores the state of collapsed items
	 */
	protected void restoreCollapsibleStates(){
		HashSet<String> collapsedItems=new HashSet<String>();
		String stateString;
		HdlDocument doc = getHdlDocument();
		
		if(doc == null){
			return;
		}
		//check for non-workspace file
		IFile file = doc.getFile();
		if (file == null)
			return;
		
		//attempt to get the files state string
		try {
			stateString=file.getPersistentProperty(VerilogPlugin.getCollapsibleId());
		} catch (CoreException e1) {
			stateString="";
		}
		
		//get a list of all the collapsed elements
		if(stateString!=null){
			for(String s:stateString.split("#")){
					collapsedItems.add(s);
			}			
		}
		//restore the collapsed state
		for(Collapsible collapsible:m_CollapsibleElements.keySet().toArray(new Collapsible[0])){
			String key=String.format("%d",collapsible.hashCode());
			if(collapsedItems.contains(key)){
				getAnnotation().collapse(m_CollapsibleElements.get(collapsible));
			}
		}		
	}
	/**
	 * This function is called to update the folding structures
	 * @param positions List of folding positions
	 */
	protected void updateFoldingStructure(Collapsible[] newCollapsibles) {			
		for (Collapsible collapsible : newCollapsibles) {
			if (m_CollapsibleElements.containsKey(collapsible)) {
				// an existing element
				ProjectionAnnotation annotation = m_CollapsibleElements
						.get(collapsible);
				Position oldPosition = getAnnotation()
						.getPosition(annotation);
				Position newPosition = getElementPosition(collapsible);
				// did the position change?
				if (oldPosition == null || !oldPosition.equals(newPosition)) {
					getAnnotation().modifyAnnotationPosition(annotation,
							newPosition);
				}
			} else {
				// if a new element was found
				ProjectionAnnotation annotation = new ProjectionAnnotation();
				Position position = getElementPosition(collapsible);
				getAnnotation().addAnnotation(annotation, position);
				// add it to the list of known collapsible
				m_CollapsibleElements.put(collapsible, annotation);
			}
		}
		// find deleted elements
		Set<Collapsible> collapsibleSet = m_CollapsibleElements.keySet();
		Iterator<Collapsible> it = collapsibleSet.iterator();
		ArrayList<Collapsible> deletedItems = new ArrayList<Collapsible>();
		while (it.hasNext()) {
			Collapsible collapsible = it.next();
			ProjectionAnnotation annotation = m_CollapsibleElements.get(collapsible);
			// if the element does not exist in the new list, remove it
			boolean bFound=false;
			for(int i=0;i<newCollapsibles.length;i++){
				if (newCollapsibles[i].equals(collapsible)){
					bFound=true;
					break;
				}
			}
			if (!bFound) {				
				getAnnotation().removeAnnotation(annotation);
				deletedItems.add(collapsible);
			} 
		}
		for (int i = 0; i < deletedItems.size(); i++) {
			m_CollapsibleElements.remove(deletedItems.get(i));
		}		
	}
	
	/**
	 * Adds an element to the list of collapsible positions
	 * 
	 * @param collapsible
	 *            the collapsible to add
	 * @param ArrayList
	 *            the list of collapsible elements
	 */
	protected void addCollapsible(Collapsible collapsible,ArrayList<Position> positions){
		
		
		HdlDocument doc=getHdlDocument();			
		int start,end;
		
		
		try {
			start=doc.getLineOffset(collapsible.startLine-1);
			end=doc.getLineOffset(collapsible.endLine);				
			positions.add(new Position(start,end-start));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}			
	}
	/**
	 * Called to get a list of collapsible positions
	 * @param outlineContainer This editor's outline container
	 * @return list of collapsible positions
	 */
	protected ArrayList<Position> getCollapsiblePositions(OutlineContainer outlineContainer){
		ArrayList<Position> positions=new ArrayList<Position>();		
		
		if(outlineContainer!=null){
			Collapsible[] collapsibleList=outlineContainer.getCollapsibleRegions();
			//loop through all the children
			for(Collapsible collapsible:collapsibleList){				
				addCollapsible(collapsible,positions);
			}
		}
		return positions;
	}
	
	/**
	 * Returns the indent string of a line number
	 * @param offset offset of a character in the line
	 * @param doc The document where the line resides
	 * @return indent string
	 */
	protected String getLineIndent(int offset){
		StringBuffer indent = new StringBuffer();
		IDocument doc=getDocument();
		
		try {
			IRegion lineRegion;
			int lineNum = doc.getLineOfOffset(offset);
			lineRegion = doc.getLineInformation(lineNum);		
			String  line=doc.get(
				lineRegion.getOffset(),
				lineRegion.getLength());
			for(int i=0;i<line.length();i++){
				Character c=line.charAt(i);
				if(Character.isSpaceChar(c)==false && c!='\t'){
					break;
				}
				indent.append(c);
			}		
		} catch (BadLocationException e) {		
			e.printStackTrace();
		}
		return indent.toString();
	}
	
	@Override
	protected boolean isTabsToSpacesConversionEnabled(){
	    boolean bUseSpaceForTab = true;           
                
        String indent = VerilogPlugin.getPreferenceString(PreferenceStrings.INDENT_TYPE);
        
        if (indent.equals(PreferenceStrings.INDENT_TAB))
            bUseSpaceForTab=false;
        return bUseSpaceForTab;
	}
	
	/**
	 * Gets a reference to the documents current annotation model
	 * @return Reference to the documents current annotation model
	 */
	protected ProjectionAnnotationModel getAnnotation(){
	    ProjectionViewer viewer =(ProjectionViewer)getSourceViewer();
	    return viewer.getProjectionAnnotationModel();
	}
	
}


