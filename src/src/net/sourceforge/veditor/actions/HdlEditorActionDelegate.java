package net.sourceforge.veditor.actions;

import net.sourceforge.veditor.editor.HdlEditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class HdlEditorActionDelegate implements IEditorActionDelegate
 {
	protected final String COMPILE="net.sourceforge.veditor.compile";
	   /**
     * Performs this action.
     * <p>
     * This method is called by the proxy action when the action has been
     * triggered. Implement this method to do the actual work.
     * </p><p>
     * <b>Note:</b> If the action delegate also implements
     * <code>IActionDelegate2</code>, then this method is not invoked but
     * instead the <code>runWithEvent(IAction, Event)</code> method is called.
     * </p>
     *
     * @param action the action proxy that handles the presentation portion of the
     *   action
     */
    public void run(IAction action){
    	
    	if(action.getId().equals(COMPILE)){
    		CompileAction compileAction=new CompileAction();
    		compileAction.run();
    	}
    }

    /**
     * Notifies this action delegate that the selection in the workbench has changed.
     * <p>
     * Implementers can use this opportunity to change the availability of the
     * action or to modify other presentation properties.
     * </p><p>
     * When the selection changes, the action enablement state is updated based on
     * the criteria specified in the plugin.xml file. Then the delegate is notified
     * of the selection change regardless of whether the enablement criteria in the
     * plugin.xml file is met.
     * </p>
     *
     * @param action the action proxy that handles presentation portion of 
     * 		the action
     * @param selection the current selection, or <code>null</code> if there
     * 		is no selection.
     */
    public void selectionChanged(IAction action, ISelection selection){
    	
    }
    
    /**
     * Sets the active editor for the delegate.  
     * Implementors should disconnect from the old editor, connect to the 
     * new editor, and update the action to reflect the new editor.
     *
     * @param action the action proxy that handles presentation portion of the action
     * @param targetEditor the new editor target
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor){
    	//FIXME: there should be a way to do this from inside the editor
    	//but until then
    	HdlEditor hdlEditor=(HdlEditor)targetEditor;
    	hdlEditor.setCurrent();
    }
}
