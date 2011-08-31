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

import net.sourceforge.veditor.document.VhdlDocumentProvider;
import net.sourceforge.veditor.editor.scanner.HdlPartitionScanner;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.MatchingCharacterPainter;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;



public class VhdlEditor extends HdlEditor
{
	static final String REPLACE_COMMENT="-- ";
	/** The editor's peer Parent Matcher */
	  private DefaultCharacterPairMatcher fParentMatcher;
	  private Color colorMatchingChar;
	  
	  /** The editor's peer character painter */
	  private MatchingCharacterPainter fMatchingCharacterPainter;
	  
	public VhdlEditor()
	{
		super();
		char[] chars = new char[] {'(', ')'};
		fParentMatcher = new DefaultCharacterPairMatcher(chars);
		setDocumentProvider(new VhdlDocumentProvider());
		setSourceViewerConfiguration(HdlSourceViewerConfiguration
				.createForVhdl(getColorManager()));
		OutlineLabelProvider = new VhdlOutlineLabelProvider();
		TreeContentProvider  = new VhdlHierarchyProvider();
	}
	
	public void createPartControl(Composite parent)
	{
	    super.createPartControl(parent);
	    
	    showMatchingCharacters();
	    
	    if (getViewer() instanceof SourceViewer) {
			SourceViewer viewer = (SourceViewer) getViewer();
	
			viewer.prependAutoEditStrategy(new CommentExtender(), HdlPartitionScanner.SINGLE_LINE_COMMENT);
		}	    
	}
	
	
	/**
	 * Class used to extend comments
	 *
	 */
	private class CommentExtender implements IAutoEditStrategy{

		public void customizeDocumentCommand(IDocument document,
				DocumentCommand command) {
			if(command.text.startsWith("\n") || command.text.startsWith("\r")){
				command.text= TextUtilities.getDefaultLineDelimiter(document) + 
				getLineIndent(command.offset)+
				REPLACE_COMMENT;
			}
			
		}
		
	}
	  /**
	   * Add a Painter to show matching characters.
	   */
	  private final void showMatchingCharacters() {
	    if (fMatchingCharacterPainter == null) {
	      if (getSourceViewer() instanceof ISourceViewerExtension2) {
	        fMatchingCharacterPainter = new MatchingCharacterPainter(
	            getSourceViewer(), fParentMatcher);
	        Display display = Display.getCurrent();
	        //IPreferenceStore store = Activator.getDefault().getPreferenceStore();
	        //colorMatchingChar = new Color(display, PreferenceConverter.getColor(
	        //    store, JJPreferences.P_MATCHING_CHAR));
	        colorMatchingChar = new Color(display,new RGB(128,128,128));
	        fMatchingCharacterPainter.setColor(colorMatchingChar);
	        ITextViewerExtension2 extension = (ITextViewerExtension2) getSourceViewer();
	        extension.addPainter(fMatchingCharacterPainter);
	      }
	    }
	  }
	  

		
	
	  

}
