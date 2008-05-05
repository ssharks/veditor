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
package net.sourceforge.veditor.document;

import java.util.Vector;

import net.sourceforge.veditor.editor.HdlPartitionScanner;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.IParser;
import net.sourceforge.veditor.parser.OutlineDatabase;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.ParserFactory;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogModuleElement;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;

public class VerilogDocument extends HdlDocument
{
	public VerilogDocument(IProject project, IFile file)
	{
		super(project, file);
	}

	public HdlPartitionScanner createPartitionScanner()
	{
		return HdlPartitionScanner.createVerilogPartitionScanner();
	}
	
	protected IParser createParser(String text)
	{
		return ParserFactory.createVerilogParser(text, getProject(), getFile());
	}
	
	public Vector<OutlineElement> getDefinitionList(String name, int offset) {
		Vector<OutlineElement> results=new Vector<OutlineElement>();
		OutlineElement   currentElement;
		
		try {			
			currentElement= getElementAt(offset,true);
			//work backwards
			while (currentElement!= null){
				OutlineElement[] children=currentElement.getChildren();
				for(int i=0;i<children.length;i++){
					if(children[i].getName().equalsIgnoreCase(name)){
						results.add(children[i]);
					}
				}
				currentElement=currentElement.getParent();
			}
			//Global definitions
			OutlineDatabase database=getOutlineDatabase();
			for(OutlineElement element: database.findTopLevelElements(name, true)){
				if (element instanceof VerilogModuleElement &&						
						//do not add the same thing twice
						results.contains(element)==false){
						
					results.add(element);
				}
			}
			
		} catch (BadLocationException e) {
		}
		catch (HdlParserException e){
			e.printStackTrace();
		}
		
		return results;
	}
	
	/**
	 * returns the context of the given offset
	 * @param documentOffset
	 * @return
	 */
	public int getContext(int documentOffset) throws BadLocationException {
		String text = get(0, documentOffset);
		IParser parser = ParserFactory.createVerilogParser(text, null, getFile());
		try {
			parser.parse();
		} catch (HdlParserException e) {
		}

		return parser.getContext();
	}
}
