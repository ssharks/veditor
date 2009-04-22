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

import net.sourceforge.veditor.editor.scanner.HdlPartitionScanner;
import net.sourceforge.veditor.editor.scanner.vhdl.VhdlPartitionScanner;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.IParser;
import net.sourceforge.veditor.parser.OutlineContainer;
import net.sourceforge.veditor.parser.OutlineDatabase;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.ParserFactory;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.ArchitectureElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.EntityDeclElement;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;

public class VhdlDocument extends HdlDocument
{
	public final static int VHDL_GLOBAL_CONTEXT=1;
	
	public VhdlDocument(IProject project, IFile file)
	{
		super(project, file);
	}

	public HdlPartitionScanner createPartitionScanner()
	{
		return new VhdlPartitionScanner();
	}
	
	protected IParser createParser(String text)
	{
		return ParserFactory.createVhdlParser(text, getProject(), getFile());
	}

	@Override
	public Vector<OutlineElement> getDefinitionList(String name, int offset) {
		Vector<OutlineElement> results=new Vector<OutlineElement>();		
		OutlineElement   currentElement;
		String entityName=null;
		
		try {
			currentElement=getElementAt(offset,true);
			//work backwards
			while (currentElement!= null){
				OutlineElement[] children=currentElement.getChildren();
				for(int i=0;i<children.length;i++){
					if(children[i].getName().equalsIgnoreCase(name)){
						results.add(children[i]);
					}
				}
				if (currentElement instanceof ArchitectureElement) {
					ArchitectureElement arch = (ArchitectureElement) currentElement;
					entityName=arch.GetEntityName();
					
				}				
				currentElement=currentElement.getParent();
			}			
			
		} catch (BadLocationException e) {
		} catch (HdlParserException e) {
			e.printStackTrace();			
		}

		if (entityName != null){
			OutlineDatabase database = getOutlineDatabase();
			OutlineContainer outline = database.getOutlineContainer(getFile());
			Object[] children= outline.getTopLevelElements();
			
			for (int i=0;i<children.length;i++){
				if (children[i] instanceof EntityDeclElement) {
					EntityDeclElement entityDecl = (EntityDeclElement) children[i];			
					//if we find an entity declaration, add the ports and generics
					if (entityDecl.getName().equalsIgnoreCase(entityName)) {				
						//get the entity's children
						OutlineElement[] enitityChildren=entityDecl.getChildren();
						for(int entChildIdx=0;entChildIdx<enitityChildren.length;entChildIdx++){
							if(	enitityChildren[entChildIdx].getName().equalsIgnoreCase(name)){
								results.add(enitityChildren[entChildIdx]);
							}
						}				
					}
				}
			}
		}
		return results;
	}
	/**
	 * returns the context of the given offset
	 * @param documentOffset
	 * @return
	 */
	public int getContext(int documentOffset) throws BadLocationException{
		return VHDL_GLOBAL_CONTEXT;
	}


}
