package net.sourceforge.veditor.actions;

import net.sourceforge.veditor.VerilogPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ISetSelectionTarget;

public class HdlShowInNavigatorAction extends  AbstractAction {
	private static final String SHOW_IN_NAVIGATOR_ACTION_IMAGE="$nl$/icons/nav.gif";
	public static final String ID="ShowInNavigator";
	
	public HdlShowInNavigatorAction() {
		super(ID);		
	}
	
	/**
	 * Gets the current active page
	 * @return
	 */
	protected IWorkbenchPage getPage(){
		IWorkbenchPage page=PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		
		return page;
	}

	@Override
	public void run() {
		IWorkbenchPage page = getPage();

		if (page == null) {
			return;
		}

		try {
			IViewPart view = page.showView(IPageLayout.ID_RES_NAV);
			if (view instanceof ISetSelectionTarget) {
				IFile file=getEditor().getHdlDocument().getFile();
				if(file != null){
					ISelection selection = new StructuredSelection(file);
					((ISetSelectionTarget) view).selectReveal(selection);
				}
			}
		} catch (PartInitException e) {

		}
	}
	
	public ImageDescriptor getImageDescriptor(){		
		return VerilogPlugin.getPlugin().getImageDescriptor(SHOW_IN_NAVIGATOR_ACTION_IMAGE);
	}
}
