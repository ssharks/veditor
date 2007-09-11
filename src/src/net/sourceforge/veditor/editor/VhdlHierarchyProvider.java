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
package net.sourceforge.veditor.editor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.OutlineDatabase;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.ArchitectureElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.ComponentInstElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.EntityDeclElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.EntityInstElement;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.VhdlOutlineElement;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class VhdlHierarchyProvider extends HdlTreeProviderBase implements
		ITreeContentProvider {
	
	private Vector<VhdlOutlineElement> m_TopLevelEntities;
	private HashMap<String,Vector<ArchitectureElement>> m_EntityArchList;
	private HashMap<String,EntityDeclElement> m_ElementDeclList;
	
	public VhdlHierarchyProvider(){
		m_TopLevelEntities	= new Vector<VhdlOutlineElement>();
		m_EntityArchList    = new HashMap<String,Vector<ArchitectureElement>>();
		m_ElementDeclList   = new HashMap<String,EntityDeclElement>();
	}

	/**
	 * Called to get a list of child elements
	 */
	public Object[] getChildren(Object parentElement) {
		//top level entity
		if (parentElement instanceof EntityDeclElement)
		{
			EntityDeclElement e = (EntityDeclElement)parentElement;
			//list of architectures
			return (Object[])m_EntityArchList.get(e.getName().toUpperCase()).toArray();
		}
		//architecture
		else if (parentElement instanceof ArchitectureElement) {
			ArchitectureElement arch = (ArchitectureElement) parentElement;
			Vector<VhdlOutlineElement> childInstantiations=new Vector<VhdlOutlineElement>();
			OutlineElement[] children= arch.getChildren();
			//return all the instantiations
			for(int i=0; i< children.length;i++){
				if (children[i] instanceof EntityInstElement) {
					//do not add recursive children
					EntityInstElement e = (EntityInstElement)children[i];
					if(e.GetEntityName().toUpperCase().equals(arch.GetEntityName().toUpperCase())==false){
						childInstantiations.add(e);
					}
				}
				if (children[i] instanceof ComponentInstElement) {
					//do not add recursive children
					ComponentInstElement comp = (ComponentInstElement)children[i];
					if(comp.GetEntityName().toUpperCase().equals(arch.GetEntityName().toUpperCase())==false){
						childInstantiations.add(comp);
					}					
				}
			}
			return (Object[]) childInstantiations.toArray();
		}
		//child instantiation
		else if (parentElement instanceof EntityInstElement) {
			EntityInstElement entityInst = (EntityInstElement) parentElement;
			String entityName= entityInst.GetEntityName().toUpperCase();
			//list of architectures for this entity
			return (Object[])m_EntityArchList.get(entityName).toArray();
		}
		else if (parentElement instanceof ComponentInstElement) {
			ComponentInstElement compInst = (ComponentInstElement) parentElement;
			String componentName= compInst.GetEntityName().toUpperCase();
			//list of architectures for this entity
			return (Object[])m_EntityArchList.get(componentName).toArray();
		}
		
		return null;
	}
	
	/**
	 * Called to get the parent for an object
	 */
	public Object getParent(Object element)
	{
		//top level entity
		if (element instanceof EntityDeclElement)
		{			
			//root entities have no parents (how sad :)
			return null;						
		}
		//architecture
		else if (element instanceof ArchitectureElement) {			
			return null;			
		}
		//child instantiation
		else if (element instanceof EntityInstElement) {			
			//list of architectures for this entity
			return null;
		}
		
		return null;
	}
	/**
	 * Called to see if an object has children
	 */
	public boolean hasChildren(Object element)
	{
		//top level entity
		if (element instanceof EntityDeclElement)
		{
			EntityDeclElement e = (EntityDeclElement)element;
			Vector<ArchitectureElement> archList=m_EntityArchList.get(e.getName().toUpperCase());
			//list of architectures
			return (archList.size() != 0);
		}
		//architecture
		else if (element instanceof ArchitectureElement) {
			ArchitectureElement arch = (ArchitectureElement) element;			
			OutlineElement[] children= arch.getChildren();			
			//return all the instantiations
			for(int i=0; i< children.length;i++){
				if (children[i] instanceof EntityInstElement) {
					EntityInstElement entityInst = (EntityInstElement)children[i];
					//if we hit one instantiation, we've got children
					//beware of recursive definitions
					if(entityInst.GetEntityName().toUpperCase().equals(arch.GetEntityName().toUpperCase()) == false){
						return true;
					}
				}
				if (children[i] instanceof ComponentInstElement) {
					ComponentInstElement componentInst = (ComponentInstElement)children[i];
					//if we hit one instantiation, we've got children
					//beware of recursive definitions
					if(componentInst.GetEntityName().toUpperCase().equals(arch.GetEntityName().toUpperCase()) == false){
						return true;
					}					
				}
			}
			return false;
		}
		//child instantiation
		else if (element instanceof EntityInstElement) {
			EntityInstElement entityInst = (EntityInstElement) element;
			String entityName= entityInst.GetEntityName().toUpperCase();
			//list of architectures for this entity
			Vector<ArchitectureElement> archList=m_EntityArchList.get(entityName);
			if(archList==null){
				return false;
			}
			return (archList.size() != 0);
		}
		else if (element instanceof ComponentInstElement) {
			ComponentInstElement compInst = (ComponentInstElement) element;
			String componentName= compInst.GetEntityName().toUpperCase();
			//list of architectures for this entity
			Vector<ArchitectureElement> archList=m_EntityArchList.get(componentName);
			if(archList==null){
				return false;
			}
			return (archList.size() != 0);
		}
		
		return false;
	}
	/**
	 * called to get a list of elements for an object
	 */
	public Object[] getElements(Object inputElement)
	{
		// parse source code and get instance list
		HdlDocument doc = (HdlDocument)inputElement;
		try {
			doc.refreshOutline();
			scanOutline(doc);
			return (Object[])m_TopLevelEntities.toArray();	
		} catch (HdlParserException e) {			
		}
		return new Object[0];
	}

	
	/**
	 * Scans the outline and builds a hierarchy tree
	 * @param doc Document used to start deriving the hierarchy
	 * @return true if the scan is successful, false otherwise
	 */
	private boolean scanOutline(HdlDocument doc){
		OutlineDatabase database;				
		Vector<ArchitectureElement> archList;
		OutlineElement[] topLevelElements;
		Set<String> entityNameList;
		Vector<String> topLevelEntities=new Vector<String>();
		String entityName;
		
		
		m_TopLevelEntities.clear();
		m_EntityArchList.clear();
		m_ElementDeclList.clear();
		try {
			database = (OutlineDatabase)
				doc.getProject().getSessionProperty(VerilogPlugin.getOutlineDatabaseId());
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
		////////////////////////////////////////////
		//scan for architectures		
		topLevelElements = database.findTopLevelElements("");
		for (int i=0;i<topLevelElements.length;i++){
			//architectures
			if (topLevelElements[i] instanceof ArchitectureElement) {
				ArchitectureElement arch = (ArchitectureElement) topLevelElements[i];
				String archEntityName=arch.GetEntityName().toUpperCase();
				//if the entity name exists, get its list otherwise create a list
				if(m_EntityArchList.containsKey(archEntityName)){					
					archList=m_EntityArchList.get(archEntityName);
				}
				else{
					//new entity
					archList=new Vector<ArchitectureElement>();
					m_EntityArchList.put(archEntityName, archList);
				}
				//add the found architecture to the list of known architectures
				archList.add(arch);
			}
			//entity declarations
			if (topLevelElements[i] instanceof EntityDeclElement) {
				EntityDeclElement entityDecl = (EntityDeclElement) topLevelElements[i];
				m_ElementDeclList.put(entityDecl.getName().toUpperCase(), entityDecl);
				if(!m_EntityArchList.containsKey(entityDecl.getName().toUpperCase())){					
					//new entity
					archList=new Vector<ArchitectureElement>();
					m_EntityArchList.put(entityDecl.getName().toUpperCase(), archList);
				}
			}
		}
		/////////////////////////////////////////////
		//Find top level entities
		entityNameList = m_EntityArchList.keySet();
		//start by assuming everything is a top level entity
		topLevelEntities.addAll(entityNameList);
		Iterator<String> it=entityNameList.iterator(); 
		while(it.hasNext()){
			entityName=it.next();
			archList=m_EntityArchList.get(entityName);
			//go through all the architectures
			for(int i=0; i<archList.size();i++){
				OutlineElement[] childElements=archList.get(i).getChildren();
				//go through all the instances
				for(int j=0;j< childElements.length; j++){
					if (childElements[j] instanceof EntityInstElement) {
						EntityInstElement entityInst = (EntityInstElement) childElements[j];
						String nameParts[] = entityInst.GetEntityName().toUpperCase().split("\\.");
						String n= 
							nameParts.length==0 ? entityInst.GetEntityName() : nameParts[nameParts.length-1];
						//remove the name from the top level
						topLevelEntities.remove(n.toUpperCase());
					}
					if (childElements[j] instanceof ComponentInstElement) {
						ComponentInstElement compInst = (ComponentInstElement) childElements[j];
						String nameParts[] = compInst.GetEntityName().toUpperCase().split("\\.");
						String n= 
							nameParts.length==0 ? compInst.GetEntityName() : nameParts[nameParts.length-1];
						//remove the name from the top level
						topLevelEntities.remove(n.toUpperCase());
					}
				}
			}
		}
		//////////////////////////////////////////
		//Start building the hierarchy		
		for (int i=0;i<topLevelElements.length;i++){
			if (topLevelElements[i] instanceof EntityDeclElement) {
				EntityDeclElement entityDecl = (EntityDeclElement) topLevelElements[i];
				//if this is a top level element, add it to the root
				if(topLevelEntities.contains(entityDecl.getName().toUpperCase())){
					m_TopLevelEntities.add(entityDecl);
				}
			}
		}
		return true;
	}
}
