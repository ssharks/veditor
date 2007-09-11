package net.sourceforge.veditor.actions;

import net.sourceforge.veditor.VerilogPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class ClearErrorMarkersAction extends AbstractAction {
	private static final String CLEAR_ERROR_MARKERS_ACTION_IMAGE="$nl$/icons/single_gray_x.gif";
	public static final String ID="ClearErrorMarkers";
	private static int m_InstallCount=0;
	
	public ClearErrorMarkersAction() {
		super(ID);
	}

	@Override
	public void run() {
		IFile file=getEditor().getHdlDocument().getFile();
			
		VerilogPlugin.clearProblemMarker(file);
	}
	
	
	public ImageDescriptor getImageDescriptor(){		
		return VerilogPlugin.getPlugin().getImageDescriptor(CLEAR_ERROR_MARKERS_ACTION_IMAGE);
	}
	
	/**
	 * Installs the action in the problems window
	 */
	public static void install(){
		//IViewPart view=getProblemsView();
		
		
		m_InstallCount++;
	}
	
	/**
	 * Removes the action from problems window
	 */
	public void unInstall(){
		
		m_InstallCount--;
	}
	
	/**
	 * Gets the current active page
	 * @return
	 */
	protected static IWorkbenchPage getPage(){
		IWorkbenchPage page=PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		
		return page;
	}
	/**
	 * Gets hold of the problems window
	 * @return
	 */
	protected static IViewPart getProblemsView(){
		IWorkbenchPage page = getPage();
		IViewPart view=null;
		
		if (page != null) {
			try {
				view = page.showView(IPageLayout.ID_PROBLEM_VIEW);				
			} catch (PartInitException e) {	
			}
		}
		return view;
	}
}
