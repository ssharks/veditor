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

import java.util.ArrayList;
import java.util.HashMap;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.parser.HdlParserException;
import net.sourceforge.veditor.parser.OutlineDatabase;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogInstanceElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogModuleElement;
import net.sourceforge.veditor.parser.verilog.VerilogOutlineElementFactory.VerilogOutlineElement;

import org.eclipse.core.runtime.CoreException;


public class VerilogHierarchyProvider extends HdlTreeProviderBase
{
	HashMap<String,VerilogModuleElement> m_ModuleList=new HashMap<String,VerilogModuleElement>();
	
	public Object[] getElements(Object inputElement)
	{
		HdlDocument doc = (HdlDocument)inputElement;
		try {
			doc.refreshOutline();
			return (Object[])scanProject(doc);
		} catch (HdlParserException e) {

		}
		return new Object[0];
	}
	
	public Object getParent(Object element)
	{
		return null;
	}

	public boolean hasChildren(Object element)
	{
		//if we have a module, it must have instatiations
		if (element instanceof VerilogModuleElement) {
			VerilogModuleElement moduleElement = (VerilogModuleElement) element;
			for(OutlineElement child : moduleElement.getChildren()){
				if(child instanceof VerilogInstanceElement){
					return true;
				}
			}
		}
		//if we have an instance, find the module that goes with it
		if (element instanceof VerilogInstanceElement) {
			VerilogInstanceElement instance = (VerilogInstanceElement) element;
			//get the module
			if(m_ModuleList.containsKey(instance.getModuleType())){
				//if the module has instantiations
				VerilogModuleElement moduleElement=m_ModuleList.get(instance.getModuleType());
				for(OutlineElement child : moduleElement.getChildren()){
					if(child instanceof VerilogInstanceElement){
						VerilogInstanceElement childInstance = (VerilogInstanceElement) child;
						//guard against recursive definitions
						if(childInstance.getModuleType().equals(moduleElement.getName()) ==false){
							return true;
						}
					}
				}
			}
		}
		return false;
	}	
	public Object[] getChildren(Object parentElement)
	{
		ArrayList<VerilogOutlineElement> children=new ArrayList<VerilogOutlineElement>();
		
		//if we have a module, it must have instatiations
		if (parentElement instanceof VerilogModuleElement) {
			VerilogModuleElement moduleElement = (VerilogModuleElement) parentElement;
			for(OutlineElement child : moduleElement.getChildren()){
				if(child instanceof VerilogInstanceElement){
					VerilogInstanceElement instance = (VerilogInstanceElement) child;
					children.add(instance);
				}
			}
		}
		//if we have an instance, find the module that goes with it
		if (parentElement instanceof VerilogInstanceElement) {
			VerilogInstanceElement instance = (VerilogInstanceElement) parentElement;
			//get the module
			if(m_ModuleList.containsKey(instance.getModuleType())){
				//if the module has instantiations
				VerilogModuleElement moduleElement=m_ModuleList.get(instance.getModuleType());
				for(OutlineElement child : moduleElement.getChildren()){
					if(child instanceof VerilogInstanceElement){
						VerilogInstanceElement childInstance = (VerilogInstanceElement) child;
						children.add(childInstance);
					}
				}
			}
		}
		
		
		return children.toArray();
	}
	
	/**
	 * Gets a list of top level modules in this project
	 * @return
	 */
	private VerilogOutlineElement[] scanProject(HdlDocument doc){
		OutlineDatabase database;
		OutlineElement []topLevelElements;
		HashMap<String,VerilogModuleElement> topLevelModules=new HashMap<String,VerilogModuleElement>();
		
		try {
			database = (OutlineDatabase)
				doc.getProject().getSessionProperty(VerilogPlugin.getOutlineDatabaseId());
		} catch (CoreException e) {
			e.printStackTrace();		
			return null;
		}
		////////////////////////////////////////////
		//scan for modules and make a list
		topLevelElements = database.findTopLevelElements("");
		m_ModuleList.clear();
		for (OutlineElement element : topLevelElements){
			if (element instanceof VerilogModuleElement) {
				VerilogModuleElement moduleElement = (VerilogModuleElement) element;
				m_ModuleList.put(moduleElement.getShortName(),moduleElement);
			}
		} 
		////////////////////////////////////////////
		// scan the modules list and remove the ones that 
		// are instatiated in another module
		topLevelModules.putAll(m_ModuleList);
		for(VerilogModuleElement module : topLevelModules.values().toArray(new VerilogModuleElement[0])){
			for(OutlineElement child: module.getChildren()){
				if (child instanceof VerilogInstanceElement) {
					VerilogInstanceElement instance = (VerilogInstanceElement) child;
					if(topLevelModules.containsKey(instance.getModuleType())){
						topLevelModules.remove(instance.getModuleType());
					}
				}
			}
		}
		
		//now what we are left with is a list of modules that are never instantiated
		return topLevelModules.values().toArray(new VerilogOutlineElement[0]);
	}
}

