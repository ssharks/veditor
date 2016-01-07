/*******************************************************************************
 * Copyright (c) 2006 Ali Ghorashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ali Ghorashi - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.parser;

import java.util.ArrayList;
import java.util.HashMap;

import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.UseClauseElement;

import org.eclipse.core.resources.IFile;

/**
 * module database for each project
 */
public class OutlineContainer
{	
	
	class Comment {
		public Comment(String s, Boolean o) {
			m_comment = s;
			m_onlycommentline = o;
		}
		public String m_comment;
		public Boolean m_onlycommentline; // true when this line contains nothing else then comment		
	}
	
	private static final int  MINIMUM_COLLAPSIBLE_SIZE=1;
	ArrayList<Collapsible> m_Collapsibles;
	HashMap<Integer,Comment > m_Comments; 
		
	
	public OutlineContainer(){
		m_Collapsibles=new ArrayList<Collapsible>();
		m_Comments=new HashMap<Integer,Comment>();
	}
	
	/**
	* Class to include all the information about a file
	*/
	private  class FileInfo{
		public OutlineElement CurrentElement;
		public ArrayList<OutlineElement> ElementList;
		/**
		* constructor
		*/
		public FileInfo(){
			ElementList=new ArrayList<OutlineElement>();
			CurrentElement = null;
		}
	}
	
	/**
	 * Class to encapsulate collapsible elements
	 */
	public class Collapsible extends Object{
		public int startLine;
		public int endLine;
		public Collapsible(int start,int end){startLine=start;endLine=end;}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj){
			if (obj instanceof Collapsible) {
				Collapsible collapsible = (Collapsible) obj;				
				if (startLine == collapsible.startLine && endLine == collapsible.endLine){return true;}		
			}
			return false;
		}
		public int hashCode(){ return String.format("%d:%d", startLine,endLine).hashCode();}
	}
	
	/** list of all the files we have seen */ 
	private  FileInfo m_FileInfo = new FileInfo();
	
	/** gets the current element scope */
	public OutlineElement GetCurrentElement(){		
		return m_FileInfo.CurrentElement;
	}
	
	/**
	 * @return a list of elements at the file level
	 */
	public OutlineElement[] getTopLevelElements(){
		return m_FileInfo.ElementList.toArray(new OutlineElement[0]);
	}

	/**
	 * Find a top level element with the given name.
	 * @param name Element name
	 * @return found element or null if not found.
	 */
	public OutlineElement findTopLevelElement(String name) {
		for (OutlineElement element : m_FileInfo.ElementList) {
			if (element.getName().equals(name)) {
				return element;
			}
		}
		return null;
	}
	
	/**
	 * Find all the use clauses in a particular file
	 * @param file The file to look in
	 * @return array of use clauses found
	 */
	public ArrayList<UseClauseElement> getUseClauses(IFile file) {
		ArrayList<UseClauseElement> foundList = new ArrayList<UseClauseElement>();
		for (OutlineElement element : m_FileInfo.ElementList) {
			if (element.getFile().equals(file)) {
				if (element instanceof UseClauseElement) {
					foundList.add((UseClauseElement)element);
				}
			}
		}
		return foundList;
	}
	
	/**
	* returns true if the given element has any children
	*/
	public boolean HasChildren(OutlineElement element){
		return element.HasChildren();
	}
	/**
	*inserts an element into the element list
	*/
	private  void InsertElement(OutlineElement element,FileInfo fileInfo){
		if (fileInfo.CurrentElement != null){
			fileInfo.CurrentElement.AddChild(element);
		}
		else{
			fileInfo.ElementList.add(element);
		}		
		fileInfo.CurrentElement = element;			
	}
	
	/**
	 * Removes all the outline elements. This function is should
	 * be called before re-parsing a file to avoid duplicate 
	 * entries
	 */
	public void clear(){
		m_FileInfo.CurrentElement = null;
		m_FileInfo.ElementList.clear();
		m_Collapsibles.clear();
		m_Comments.clear();
	}
	/**
	* This function is called to mark the beginning of an
	* element
	* @param elementName name of the element
	  @param type type of the element
	  @param line ending line
	  @param filename filename in which the element lives
	  @return newly created element
	*/
	public OutlineElement beginElement(String elementName,
				String type,
				int line, 
				int col,
				IFile file,
				OutlineElementFactory elementFactory){
				
		OutlineElement element = 
			elementFactory.CreateElement(elementName,type,line,col,java.lang.Integer.MAX_VALUE,java.lang.Integer.MAX_VALUE,file);
		//put the element into the element list
		InsertElement(element,m_FileInfo);
		return element;
	}
	
	/**
	 * This function is called to mark the ending of an element	
	 * @param elementName name of the element
	 * @param type type of the element
	 * @param line ending line
	 * @param filename filename in which the element lives
	 * @return true if successful, false otherwise
	 */
	public  boolean endElement(String elementName, String type,int line,int col, IFile file){		
		if (m_FileInfo.CurrentElement == null){
			//huh?? encountered the end of an element without seeing the beginning
			return false;
		}
		else{
			//sanity check
			if (!m_FileInfo.CurrentElement.getName().equals(elementName) ||
				!m_FileInfo.CurrentElement.getFile().equals(file)) {
				//something terribly wrong
				return false;
			}
			else{
				m_FileInfo.CurrentElement.setEndingLine(line);
				m_FileInfo.CurrentElement.setEndingCol(col);
				m_FileInfo.CurrentElement = m_FileInfo.CurrentElement.getParent();
			}					
		}			
		return true;
	}
	
	/**
	 * Adds the position to the list of collapsible positions
	 * @param position
	 */
	public void addCollapsibleRegion(Collapsible collapsible){
		if(collapsible.endLine - collapsible.startLine > MINIMUM_COLLAPSIBLE_SIZE){
			m_Collapsibles.add(collapsible);
		}
	}
	
	/**
	 * 
	 * @return A list of collapsible regions
	 */
	public Collapsible[] getCollapsibleRegions(){
		return m_Collapsibles.toArray(new Collapsible[0]);
	}
	
	/**
	 * Adds a string to the comments list
	 * @param endLine Ending line of the comment
	 * @param text The comment text
	 */
	public void addComment(int endLine,String text, Boolean onlycomment){
		m_Comments.put(endLine,new Comment(text,onlycomment));
	}
	
	/**
	 * Gets the comments near the given element. These comments will 
	 * presumably be associated with the element
	 * @param element the element to look around
	 * @return Comment string
	 */
	public String getCommentsNear(OutlineElement element){
		int startLine=element.getStartingLine();
		//look on the same line
		if(m_Comments.containsKey(startLine)){
			return m_Comments.get(startLine).m_comment;
		}
		// look on previous lines:
		String comment = "";
		for(int line = startLine-1;;line--) {
			if(!m_Comments.containsKey(line)) break;
			if(!m_Comments.get(line).m_onlycommentline) break;
			comment = m_Comments.get(line).m_comment+"\n"+comment;
		}	
		return comment;
	}
	
	
	/**
	 * Decides whether the give line and column is inside the element
	 * @param e
	 * @param line
	 * @param col
	 * @return
	 */
	private boolean isPositionInsideElement(OutlineElement e,int line,int col){
		//easy case
		if(line > e.getStartingLine() && line < e.getEndingLine()){
			return true;
		}
		//special case
		//if the line and beginning line of the element are on the same line
		if(line == e.getStartingLine() && col > e.getStartingCol()){
			//then the starting column must be after the element's column
		}
		else{
			return false;
		}
		//if the end line is the same as the elements end line
		if(line == e.getEndingCol() && col < e.getEndingCol()){
			//then the column must be before the element's end column
			return true;
		}
		
		return false;
	}
	/**
	 * Recursively scans through the element and its children to find the
	 * deepest element surrounding the given line and column
	 * @param element
	 * @param line
	 * @param col
	 * @return The deepest element surrounding the line. null if no element 
	 * surrounds the given line
	 */
	private OutlineElement getContext(OutlineElement element,int line,int col){
		OutlineElement results=null;
		if(isPositionInsideElement(element, line, col)){
			//is the line enclosed in one of the children
			for(OutlineElement child:element.getChildren()){
				results=getContext(child,line,col);
				if(results!=null){
					return results;
				}
			}
			//if not in children, then this element is the parent
			return element;
		}
		//this element does not enclose the line
		return null;
	}
	/**
	 * This function returns the context of the given line and column
	 * @param line the line number for the context
	 * @param col the column number for the context
	 * @return OutlineElement of the context enclosing the specified 
	 * line. null if the line is in file scope
	 */
	public OutlineElement getLineContext(int line,int col){
		OutlineElement results=null;
		OutlineElement[] topLevelElements=getTopLevelElements();
		
		for(OutlineElement element:topLevelElements){
			results=getContext(element,line,col);
			if(results!=null){
				break;
			}
		}
		
		return results;
	}
}	