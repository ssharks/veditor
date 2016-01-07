package net.sourceforge.veditor.editor;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.parser.VariableStore;
import net.sourceforge.veditor.preference.PreferenceStrings;

import org.eclipse.jface.text.IDocument;
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
			// ToDo: It should consider variable scope and read or write.
			TextSelection textSelection = (TextSelection)selection;
			if ((textSelection.getLength() > 0) &&
					(VerilogPlugin.getPreferenceBoolean( PreferenceStrings.MARK_SELECTION_OCCURENCES ))) { // skip single character selections
				VariableStore store = fEditor.getHdlDocument().getVariableStore();
				ArrayList<Integer> findList = new ArrayList<Integer>();
				ArrayList<Integer> writeList = new ArrayList<Integer>();
				
				// search for all occurrences and annotate them
				if (store == null) {
					String text = fEditor.getViewer().getDocument().get();
					findListSimple(findList, textSelection.getText(), text);
				} else {
					findListFromVariableStore(findList, writeList, textSelection, store);
				}
				
				// for single finds do not highlight
				int length = textSelection.getLength();
				int count = findList.size() + writeList.size();
				if (count > 1&& findList.size() < maxHits) {
					for (int i=0;i < findList.size(); i++) {
						Annotation annotation = new Annotation( "net.sourceforge.veditor.occurrences", false, "Description" );
						model.addAnnotation(annotation, new Position(findList.get(i), length));
						annotationArray.add(annotation);
					}
				}
				if (count > 1 && writeList.size() < maxHits) {
					for (int i=0;i < writeList.size(); i++) {
						Annotation annotation = new Annotation( "net.sourceforge.veditor.occurrences.write", false, "Description" );
						model.addAnnotation(annotation, new Position(writeList.get(i), length));
						annotationArray.add(annotation);
					}
				}
			}
		}
	}
	
	private void findListFromVariableStore(
			List<Integer> findList, List<Integer> writeList, 
			TextSelection selection, VariableStore store) {
		IDocument doc = fEditor.getViewer().getDocument();
		store.findOccurrenceList(findList, writeList, selection.getText(), selection.getStartLine(), doc);
	}

	private void findListSimple(List<Integer> findList, String selText, String text) {
		int length = selText.length();
		int lastIndex = 0;
		do {
			lastIndex = text.indexOf(selText, lastIndex);
			if (lastIndex != -1) {
				if (isIdentifier(text, length, lastIndex)) {
					findList.add(lastIndex);
				}
				lastIndex += length;
				// stop on too much hits
				if (findList.size() >= maxHits) {
					break;
				}
			}
		} while (lastIndex != -1);
	}

	/**
	 * test identifier.
	 * note: occurrence must be identifier
	 */
	private boolean isIdentifier(String text, int length, int index) {
		try {
			char prev = text.charAt(index - 1);
			char next = text.charAt(index + length);
			for (int i = 0; i < length; i++) {
				if (Character.isJavaIdentifierPart(text.charAt(index + i)) == false) {
					return false;
				}
			}
			// avoid part of other identifier
			if (Character.isJavaIdentifierPart(prev))
				return false;
			if (Character.isJavaIdentifierPart(next))
				return false;
			return true;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}
}
