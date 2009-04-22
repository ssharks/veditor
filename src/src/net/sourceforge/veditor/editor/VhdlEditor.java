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
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;



public class VhdlEditor extends HdlEditor
{
	static final String REPLACE_COMMENT="-- ";
	
	public VhdlEditor()
	{
		super();
		setDocumentProvider(new VhdlDocumentProvider());
		setSourceViewerConfiguration(HdlSourceViewerConfiguration
				.createForVhdl(getColorManager()));
		OutlineLabelProvider = new VhdlOutlineLabelProvider();
		TreeContentProvider  = new VhdlHierarchyProvider();
	}
	
	public void createPartControl(Composite parent)
	{
	    super.createPartControl(parent);
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
				command.text="\n" + 
				getLineIndent(command.offset)+
				REPLACE_COMMENT;
			}
			
		}
		
	}
}
