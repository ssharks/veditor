package net.sourceforge.veditor.editor;

import java.util.ArrayList;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.preference.PreferenceStrings;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

public class MarkSelectionOccurences implements ISelectionChangedListener {
	protected HdlEditor fEditor;
	protected ArrayList<Annotation> annotationArray;
	protected IAnnotationModel lastModel;
	final protected int maxHits = 100;
	
	public MarkSelectionOccurences(HdlEditor editor) {
		fEditor = editor;
		annotationArray = new ArrayList<Annotation>();
		lastModel = null;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		
		// remove all items in the list
		if (lastModel != null) {
			int index = 0;
			for (index = 0; index < annotationArray.size(); index++) {
				Annotation annotation = annotationArray.get(index);
				Position pos = lastModel.getPosition(annotation);
				if (pos != null) {
					pos.delete();
					lastModel.removeAnnotation(annotation);
				}
			}
			lastModel = null;
			annotationArray.clear();
		}
		
		IAnnotationModel model = fEditor.getDocumentProvider().getAnnotationModel( fEditor.getEditorInput() );
		lastModel = model;
		
		if (selection instanceof TextSelection) { 
			TextSelection textSelection = (TextSelection)selection;
			if ((textSelection.getLength() > 1) &&
					(VerilogPlugin.getPreferenceBoolean( PreferenceStrings.MARK_SELECTION_OCCURENCES ))) { // skip single character selections
				String text = fEditor.getViewer().getDocument().get();
				String selText = textSelection.getText();
				ArrayList<Integer> findList = new ArrayList<Integer>();
				
				// search for all occurences and annotate them
				int lastIndex = 0;
				do {
					lastIndex = text.indexOf(selText,lastIndex);
					if( lastIndex != -1){
						findList.add(lastIndex);
						lastIndex+=selText.length();
						
						// stop on too much hits
						if (findList.size() >= maxHits) {
							break;
						}
					}
				} while(lastIndex != -1);
				
				// for single finds do not highlight
				if ((findList.size() > 1) && (findList.size() < maxHits)) {
					for (int i=0;i < findList.size(); i++) {
						Annotation annotation = new Annotation( "org.eclipse.jdt.ui.occurrences", false, "Description" );
						model.addAnnotation( annotation, new Position( findList.get(i), textSelection.getLength() ) );
						annotationArray.add(annotation);
					}
				}
			}
		}
	}

}
