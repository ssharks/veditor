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
package net.sourceforge.veditor.parser.verilog;

import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.OutlineElementFactory;

import org.eclipse.core.resources.IFile;

/** 
 * Class factory for Verilog Outline Elements
 *
 */
public class VerilogOutlineElementFactory extends OutlineElementFactory {
	// type checkers
	private boolean isModule(String type){return type.startsWith("module#");	}
	private boolean isTask(String type)  { return type.startsWith("task#");	}
	private boolean isFunction(String type)  { return type.startsWith("function#");	}
	private boolean isInstance(String type) {return  type.startsWith("instance#"); }
	private boolean isParameter(String type)
	{
		return type.startsWith("parameter#") || type.startsWith("localparam#");
	}
	private boolean isPort(String type)      {return  type.startsWith("port#"); }
	private boolean isSignal(String type)    {return  type.startsWith("variable#"); }
	private boolean isRegister(String type)    {return  type.startsWith("variable#reg#"); }
	private boolean isWire(String type)    {return  type.startsWith("variable#wire#"); }
	
	/**
	 * Outline element classes
	 */
	public class VerilogOutlineElement extends OutlineElement{
		protected String m_ShortName;
		protected String m_LongName;
		protected String m_ImageName;
		protected String[] m_TypeParts;
		public VerilogOutlineElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_TypeParts=m_Type.split("#");
			m_ShortName= m_Name;
			if(m_TypeParts.length > 0){
				m_LongName =  m_Name+" : "+m_TypeParts[0];
			}
			else{
				m_LongName =  m_Name+" : "+m_Type;
			}
			m_ImageName="$nl$/icons/obj.gif";
		}
		public String GetImageName(){
			return m_ImageName;
		}
		public String getShortName(){
			return m_ShortName;
		}
		public String getLongName(){
			return m_LongName;
		}
	}
	public class VerilogModuleElement extends VerilogOutlineElement{
		public VerilogModuleElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);					
			m_ImageName="$nl$/icons/module.gif";
		}
	}
	public class VerilogTaskElement extends VerilogOutlineElement{
		public VerilogTaskElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);					
			m_ImageName="$nl$/icons/t.gif";
		}
	}
	public class VerilogInstanceElement extends VerilogOutlineElement{
		
		public VerilogInstanceElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);					
			m_ImageName="$nl$/icons/yello_dia.gif";
			
			m_LongName=String.format("%s(%s)",name,getModuleType());
			
		}
		
		/**
		 * Returns the type of this module instantiation
		 * @return
		 */
		public String getModuleType(){
			if(m_TypeParts.length > 1){
				return m_TypeParts[1]; 
			}
			else{
				return "";
			}
		}
	}
	public class VerilogFunctionElement extends VerilogOutlineElement{
		public VerilogFunctionElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);					
			m_ImageName="$nl$/icons/f.gif";
		}
	}
	public class VerilogPortElement extends VerilogOutlineElement{
		public static final int INPUT	=0;
		public static final int OUTPUT	=1;
		public static final int INOUT	=2;
		private int m_Direction;
		
		public String GetDirectionString(){
			switch(m_Direction){
			case INPUT:
				return "in";
			case OUTPUT:
				return "out";
			case INOUT:
				return "inout";
			default:
				return "";
			}
		}
		public VerilogPortElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);					
			if(type.toLowerCase().startsWith("port#input#")){
				m_Direction=INPUT;
				m_ImageName="$nl$/icons/port_in.gif";
			}
			else if (type.toLowerCase().startsWith("port#output#")){
				m_Direction = OUTPUT;
				m_ImageName="$nl$/icons/port_out.gif";
			}
			else{
				m_Direction = INOUT;
				m_ImageName="$nl$/icons/port_inout.gif";
			}
			m_LongName = String.format("%s :", name);
			for (int i = 1; i < m_TypeParts.length; i++) {
				if (i >= 4)
					break; // remove last "cstyle"
				m_LongName += String.format(" %s", m_TypeParts[i]);
			}
		}
	}
	public class VerilogParameterElement extends VerilogOutlineElement{
		public VerilogParameterElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/P.gif";
			String value = GetValue();
			m_LongName = String.format("%s : %s %s", name, m_TypeParts[0], value);
		}
		
		public boolean isLocal() {
			return m_Type.startsWith("localparam#");
		}
		
		public String GetValue(){
			if(m_TypeParts.length > 3)
				return m_TypeParts[3];			
			else
				return "";
		}
	}
	public class VerilogSignalElement extends VerilogOutlineElement{		
		public VerilogSignalElement(String name, String type, int startLine, int startCol, int endLine, int endCol, IFile file, boolean bVisible) {
			super(name, type, startLine, startCol, endLine, endCol, file, bVisible);
			m_ImageName = "$nl$/icons/signal.gif";
			m_LongName = String.format("%s :", name);
			int len = m_TypeParts.length;
			boolean isVariable = m_TypeParts[0].equals("variable");
			if (isVariable && len > 3) {
				len = 3;
			}
			for (int i = 1; i < len; i++) {
				m_LongName += String.format(" %s", m_TypeParts[i]);
			}
			if (isVariable && m_TypeParts.length >= 3) {
				int dim = Integer.parseInt(m_TypeParts[3]);
				for (int i = 0; i < dim; i++) {
					m_LongName += "[]";
				}
			}
		}
	}

	public class VerilogWireElement extends VerilogSignalElement{		
		public VerilogWireElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);			
			m_ImageName="$nl$/icons/wire_signal.gif";						
		}	
	}
	public class VerilogRegElement extends VerilogSignalElement{		
		public VerilogRegElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);			
			m_ImageName="$nl$/icons/register_signal.gif";						
		}	
	}
	public class VerilogGroupElement extends VerilogSignalElement{		
		public VerilogGroupElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);			
			m_ImageName="$nl$/icons/obj.gif";						
		}	
	}
	
	/**
	 * Class factory function
	 */
	public OutlineElement CreateElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file){
		if (isModule(type)){
			return new VerilogModuleElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isTask(type)){
			return new VerilogTaskElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isFunction(type)){
			return new VerilogFunctionElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isInstance(type)){
			return new VerilogInstanceElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isPort(type)){
			return new VerilogPortElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isParameter(type)){
			return new VerilogParameterElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isWire(type)){
			return new VerilogWireElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isRegister(type)){
			return new VerilogRegElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isSignal(type)){
			return new VerilogSignalElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}

		return new VerilogOutlineElement(name,type,startLine,startCol,endLine,endCol,file,true);
	}
}
