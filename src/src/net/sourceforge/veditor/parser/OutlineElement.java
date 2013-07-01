/*******************************************************************************
 * Copyright (c) 2007 Ali Ghorashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ali Ghorashi - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFile;

/**
* Class to describe the element and its children
*/
public class OutlineElement extends Object{
	protected String m_Name;
	protected String m_HashString;
	protected IFile m_File;
	protected int m_StartingLine, m_StartingCol;
	protected int m_EndingLine, m_EndingCol;
	protected List<OutlineElement> m_Children;	
	protected OutlineElement m_Parent;
	protected String m_Type;
	protected boolean m_bIsVisible;
	
	public String GetImageName(){
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj){
		if (obj instanceof OutlineElement) {
			OutlineElement outlineElem = (OutlineElement) obj;
			
			if (outlineElem.m_HashString.equals(m_HashString)){
				return true;
			}		
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode(){		
		return m_HashString.hashCode();		
	}
	/**
	 * Constructor
	 * @param name element name
	 * @param startLine element's starting line
	 * @param endLine The element's ending line
	 * @param file The filename the element lives in
	 * @param bVisible If true, this element will appear in the outline, if false it will be used for
	 * internal bookkeeping only
	 */
	public OutlineElement(String name,
			String type,
			int startLine,
			int startCol,
			int endLine,
			int endCol,
			IFile file,
			boolean bVisible){
		m_Name = name;
		m_StartingLine = startLine;
		m_StartingCol= startCol;
		m_EndingLine = endLine;
		m_EndingCol = endCol;
		m_bIsVisible=bVisible;
		m_File = file;
		m_Children = new ArrayList<OutlineElement>();
		// all children are created as orphans :)
		m_Parent = null;
		m_Type = type;
		m_HashString = m_Name+"#"+type;
	}
	
	public boolean isVisible(){
		return m_bIsVisible;
	}
	
	public String toString()
	{
		return m_Name;
	}
	/**
	* Adds an element to the child list
	* @param element The element to add
	*/
	public void AddChild(OutlineElement element){
		element.m_Parent = this;
		m_Children.add(element);
	}
	/**
	 *@return True if the element has any children
	 */
	public boolean HasChildren(){
		return (m_Children.size()!= 0);
	}
	/**
	* Determines if an element is child of another
    */
	public boolean IsAChildOf(OutlineElement child){
		// both modules must live in the same file
		if ( !m_File.equals(child.getFile())){
			return false;
		}
		
		return false;
	}
	/**
	 * @return The name of this element
	 */
	public String getName() {
		return m_Name;
	}
	
	/**
	 * @return The name of this element with all the decorations
	 */
	public String getLongName(){
		return m_Name;
	}
	
	public String getFullSourceCode(){
		try {
			InputStream is = m_File.getContents(true);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			int line=1;
			for(;line < m_StartingLine;line++)	br.readLine();
			StringBuffer sb = new StringBuffer();
			
			Vector<String> lines = new Vector<String>();
			lines.add(br.readLine()); line++;
			
			for(;line <= m_EndingLine;line++)	{
				lines.add(br.readLine());
			}
			br.close();
			is.close();
			
			String firstline = lines.elementAt(0);
			int wordstart = 0;
			for(;wordstart < firstline.length();wordstart++){
				char c = firstline.charAt(wordstart);
				if(c==9 || c==32)continue;
				break;
			}
			
			String whitespace = firstline.substring(0,wordstart);
			
			for(int i=0; i<lines.size();i++){
				String a = lines.elementAt(i);
				String b = a;
				if(wordstart != 0){
					if(a.startsWith(whitespace))b = a.substring(wordstart);
				}
				if(i!=0)sb.append("\n");
				sb.append(b);
			}
			
			
			return sb.toString().trim();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	

	/**
	 * @return The name of this element with minimum decorations
	 */
	public String getShortName(){
		return m_Name;
	}
	/**
	 * @return Number of lines between begin and end
	 */
	public int getLength(){
		return m_EndingLine - m_StartingLine;
	}
	public void setName(String name) {
		m_Name = name;
		m_HashString = m_Name+"#"+m_Type;
	}
	public IFile getFile() {
		return m_File;
	}
	public void setFile(IFile file) {
		m_File = file;
	}
	public int getStartingLine() {
		return m_StartingLine;
	}
	public int getEndingLine() {
		return m_EndingLine;
	}
	public void setEndingLine(int endingLine) {
		m_EndingLine = endingLine;
	}		
	public void setEndingCol(int col){m_EndingCol=col;}
	public int  getEndingCol(){return m_EndingCol;}
	public int  getStartingCol(){return m_StartingCol;} 
	
	public OutlineElement getParent() {
		return m_Parent;
	}
	public void setParent(OutlineElement parent) {
		m_Parent = parent;
	}

	public OutlineElement[] getChildren() {
		return m_Children.toArray(new OutlineElement[0]);
	}
	
	public OutlineElement findChild(String name) {
		for (OutlineElement child : m_Children) {
			if (child.getName().equals(name)) {
				return child;
			}
		}
		return null;
	}

	public OutlineElement getChild(int index) {
		if (index < m_Children.size())
			return m_Children.get(index);
		else
			return null;
	}

	public String getType() {
		return m_Type;
	}
	
}
