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
package net.sourceforge.veditor.parser.vhdl;

import java.util.ArrayList;

import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.parser.OutlineElementFactory;

import org.eclipse.core.resources.IFile;

/** 
 * Class factory for Vhdl Outline Elements
 *
 */
public class VhdlOutlineElementFactory extends OutlineElementFactory {
	
    private boolean isArchitecture (String type){return type.startsWith("architecture#");}
    private boolean isPackageBody  (String type)  {return type.equals("packageBody#");   }
    private boolean isPackageDecl  (String type)  {return type.equals("packageDecl#");   }
    private boolean isFunction     (String type) { return type.startsWith("function#"); }
    private boolean isFunctionDef  (String type) { return type.startsWith("functionDecl#"); }
    private boolean isProcedureDef  (String type) { return type.startsWith("procedureDecl#"); }
    private boolean isProcess      (String type){return type.startsWith("process#"); }
    private boolean isProcedure    (String type){return type.startsWith("procedure#"); }
    private boolean isComponentDecl(String type){return type.startsWith("componentDecl#"); }
    private boolean isComponentInst(String type){return type.startsWith("componentInst#"); }
    private boolean isEntityDecl   (String type){return type.startsWith("entityDecl#"); }
    private boolean isEntityInst   (String type){return type.startsWith("entityInst#"); }
    private boolean isUseClause    (String type){return type.startsWith("useClause#"); }
    private boolean isPort         (String type){return type.toLowerCase().startsWith("port#"); }
    private boolean isGeneric      (String type){return type.toLowerCase().startsWith("generic#"); }
    private boolean isSignal       (String type){return type.toLowerCase().startsWith("signal#"); }
    private boolean isVariable     (String type){return type.toLowerCase().startsWith("variable#"); }  
    private boolean isConstant     (String type){return type.toLowerCase().startsWith("constant#"); }  
    private boolean isAlias        (String type){return type.toLowerCase().startsWith("alias#"); }
    private boolean isFile         (String type){return type.toLowerCase().startsWith("file#"); }
    private boolean isRecordMember (String type){return type.toLowerCase().startsWith("recordmember#"); }
    private boolean isTypeDecl     (String type){return type.toLowerCase().startsWith("type#"); }
    private boolean isSubTypeDecl  (String type){return type.toLowerCase().startsWith("subtypedecl#"); }
	
	/**
	 * All VHDL outline classes are derived from this one;
	 *
	 */
	public class VhdlOutlineElement extends OutlineElement{
		protected String m_ShortName;
		protected String m_LongName;
		protected String m_ImageName;
		protected String[] m_TypeParts;
		public VhdlOutlineElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ShortName= m_Name;
			m_LongName =  m_Name+" : "+m_Type;
			m_TypeParts=m_Type.split("#");
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
		public String getText(){
			return m_ShortName;
		}
		public String getTypePart1(){
			if(m_TypeParts.length > 1){
				return m_TypeParts[1];
			}
	         return "";
		}
		public String getTypePart2(){
			if (m_TypeParts.length > 2){
				return m_TypeParts[2];
			}	else 
				return "";
		}
	}	
	/**
	 * Element type classes
	 * @note type string: architecture#[EntityName]
	 */
	public class ArchitectureElement extends VhdlOutlineElement{
		public ArchitectureElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/arch.gif";			
			m_LongName = String.format("architecture(%s) of %s", name,GetEntityName());
			m_ShortName=m_LongName;
		}	
		public String GetEntityName(){			
			if (m_TypeParts.length > 1){
				return m_TypeParts[1];
			}
			else 
				return "";
		}
	}
	/**  @note type string: packageBody# */
	public class PackageBodyElement extends VhdlOutlineElement{
		public PackageBodyElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/package_body.gif";
			m_LongName = name+": Package Body";
			m_ShortName=m_LongName;
		}	
	}
	/**  @note type string: packageDecl# */
	public class PackageDeclElement extends VhdlOutlineElement{
		public PackageDeclElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/package_decl.gif";
			m_LongName=name+": Package Decl";
			m_ShortName=name+" (pkg)";
		}
	}
	/** This class is not meant to be instantiated directly */
	public class VhdlSubprogram  extends VhdlOutlineElement{
		protected VhdlSubprogram(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			
			m_ShortName=m_Name;
			
			//set the long name			
			Parameter[] params=getParameters();
			if(params.length >1){
				StringBuffer buffer=new StringBuffer(name);
				buffer.append("(");
				int lastComma=0;
				for(Parameter param:params){
					buffer.append(param.m_Name);
					buffer.append(" : ");
					buffer.append(param.m_Direction);
					buffer.append(" ");
					buffer.append(param.m_Type);
					lastComma=buffer.length();
					buffer.append(", ");
				}
				buffer.delete(lastComma, lastComma+1);
				buffer.append(")");
				m_LongName=buffer.toString();
			}
			else{
				m_LongName=name;
			}
		}		
		
		/**
		 * Gets a list of parameters
		 * @return List of parameters for this sub program
		 */
		public Parameter[] getParameters(){
			int idx;
			ArrayList <Parameter> results=new ArrayList<Parameter>();
			
			for(idx=1;idx+2<m_TypeParts.length;idx+=3){
				Parameter param=new Parameter();
				param.m_Name=m_TypeParts[idx];
				param.m_Type=m_TypeParts[idx+1];
				param.m_Direction=m_TypeParts[idx+2];
				results.add(param);
			}					
			return results.toArray(new Parameter[0]);
		}		
		
		/** This class encapsulates parameter information */
		public class Parameter{			
			public String m_Name="";
			public String m_Type="";
			public String m_Direction="in";			
		}
	}
	
	/**  @note type string: function#[Parameters:name#type#direction]#return type */
	public class FunctionElement extends VhdlSubprogram{
		public FunctionElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/f.gif";
			
			m_LongName+="return "+getType();
		}	
		
		public String getReturnType(){
			//the return type is the last field
			if(m_TypeParts.length>0){
				return m_TypeParts[m_TypeParts.length-1];
			}
			return "";
		}
	}
	/**  @note type string: functionDecl#[Parameters:name#type#direction]#return type */
	public class FunctionDefElement extends VhdlSubprogram{
		public FunctionDefElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/f_def.gif";
			
			m_LongName+="return "+getType();
		}		
		
		public String getReturnType(){
			//the return type is the last field
			if(m_TypeParts.length>0){
				return m_TypeParts[m_TypeParts.length-1];
			}
			return "";
		}
		public String getTypePartArgu(){
			String replace="";
		if (m_TypeParts.length>0){	
		int i=	m_TypeParts.length/3;
		for (int j=0;j<i;j++){
			
			replace	+=m_TypeParts[j*3+1]+" "+m_TypeParts[j*3+2]+",";
			}
		replace=replace.substring(0, replace.length()-1);
			return replace;
			
		}
		return replace;
		}
	}
	/**  @note type string: procedure#[Parameters:name#type#direction] */
	public class ProcedureElement extends VhdlSubprogram{
		public ProcedureElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/P.gif";		
		}		
	}
	/**  @note type string: procedureDecl#[Parameters:name#type#direction] */
	public class ProcedureDefElement extends VhdlSubprogram{
		public ProcedureDefElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/P.gif";		
		}		
	}
	/**  @note type string: process#*/
	public class ProcessElement extends VhdlOutlineElement{
		public ProcessElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/process.gif";
			m_LongName=name+": process";
			m_ShortName=m_LongName;
		}		
	}	
	/**  @note type string: componentInst#[entity name] */
	public class ComponentInstElement extends VhdlOutlineElement{
		public ComponentInstElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/green_tri.gif";			
			m_LongName=String.format("%s : %s",name,GetEntityName());
			m_ShortName=m_LongName;
		}
		public String GetEntityName(){			
			if (m_TypeParts.length > 1){
				return m_TypeParts[1];
			}
			else 
				return "";
		}
	}
	/**  @note type string: componentDecl# */
	public class ComponentDeclElement extends VhdlOutlineElement{
		public ComponentDeclElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/blue_tri.gif";
			m_LongName=name+" : component";
			m_ShortName=name+" (cmp)";
		}		
	}
	/**  @note type string: entityInst#[entity name] */
	public class EntityInstElement extends VhdlOutlineElement{
		public EntityInstElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/gray_circ.gif";
			m_LongName=name+" : "+GetEntityName();
			m_ShortName=m_LongName;
		}
		public String GetEntityName(){			
			if (m_TypeParts.length > 1){
				return m_TypeParts[1];
			}
			else 
				return "";
		}
	}
	/**  @note type string: entityDecl# */
	public class EntityDeclElement extends VhdlOutlineElement{
		public EntityDeclElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/gray_circ_small.gif";
			m_LongName=name+" : entity" ;
			m_ShortName=name+" (ent)";
		}		
	}
	/**  @note type string: generic#[type] */
	public class GenericElement extends VhdlOutlineElement{
		public GenericElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/generic.gif";
			m_LongName=name+" : ";
			if(m_TypeParts.length > 1){
				m_LongName += m_TypeParts[1];
			}
			m_ShortName=m_LongName;
		}		
	}
	
	/**  @note type string: useClause#[type] */
	public class UseClauseElement extends VhdlOutlineElement{
		public UseClauseElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_LongName=name +" (use)";
			
			m_ShortName=m_LongName;
		}		
	}
	
	/**  @note type string: signal#[type] */
	public class VhdlSignalElement extends VhdlOutlineElement{
		public VhdlSignalElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/signal.gif";
			m_LongName=name + " : " + GetSignalType() + getInitialValue();
			m_ShortName=m_LongName;
		}
		public String GetSignalType(){
			if(m_TypeParts.length > 1){
				return m_TypeParts[1];
			}
			return "";
		}

		public String getInitialValue() {
			if (m_TypeParts.length > 2) {
				String m = ":=" + m_TypeParts[2];
				return m;
			}
			return "";
		}
	}
	/**  @note type string: variable#[type] */
	public class VariableElement extends VhdlSignalElement{
		public VariableElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/variable_signal.gif";				
		}
	}
	/**  @note type string: alias#[type] */
	public class AliasElement extends VhdlSignalElement{
		public AliasElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/alias_signal.gif";				
		}
	}
	/**  @note type string: constant#[type] */
	public class ConstantElement extends VhdlSignalElement{
		public ConstantElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/constant_signal.gif";				
		}
	}
	/**  @note type string: file# */
	public class FileElement extends VhdlSignalElement{
		public FileElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/ar_obj.gif";				
		}
	}
	public class TypeDecl extends VhdlOutlineElement{
        public TypeDecl(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
            super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
            m_ImageName="$nl$/icons/type.gif";                
        }
    }
	public class SubTypeDecl extends VhdlOutlineElement{
        public SubTypeDecl(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
            super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
            m_ImageName="$nl$/icons/type.gif";                
        }
    }
	/**  @note type string: port#[in,out,buffer,inout]#[type] */
	public class VhdlPortElement extends VhdlOutlineElement{
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
		public VhdlPortElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			if(type.toLowerCase().startsWith("port#in#")){
				m_Direction=INPUT;
				m_ImageName="$nl$/icons/port_in.gif";
			}
			else if (type.toLowerCase().startsWith("port#out#")){
				m_Direction = OUTPUT;
				m_ImageName="$nl$/icons/port_out.gif";
			}
			else{
				m_Direction = INOUT;
				m_ImageName="$nl$/icons/port_inout.gif";
			}	
			m_LongName=String.format("Port %s %s : %s", GetDirectionString(),name,m_TypeParts[2]);
			m_ShortName=String.format("%s : %s", name,m_TypeParts[2]);
		}		
	}
	/**  @note type string: record# */
	public class RecordMemberElement extends VhdlOutlineElement{
		public RecordMemberElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file,boolean bVisible){
			super(name,type,startLine,startCol,endLine,endCol,file,bVisible);
			m_ImageName="$nl$/icons/obj.gif";		
		}
	}
	
	/**
	 * Class Factory function
	 */
	public OutlineElement CreateElement(String name,String type,int startLine,int startCol,int endLine,int endCol,IFile file){		
		if(isArchitecture(type)){
			return new ArchitectureElement(name,type,startLine,startCol,endLine,endCol,file,true);
		} else if (isPackageBody(type)){
			return new PackageBodyElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}else if (isPackageDecl(type)){
			return new PackageDeclElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}else if (isFunction(type)){
			return new FunctionElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}else if (isFunctionDef(type)){
			return new FunctionDefElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}else if (isProcedureDef(type)){
			return new ProcedureDefElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}else if (isProcess(type)){
			return new ProcessElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}else if (isProcedure(type)){
			return new ProcedureElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}else if (isComponentDecl(type)){
			return new ComponentDeclElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}else if (isComponentInst(type)){
			return new ComponentInstElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}else if (isEntityDecl(type)){
			return new EntityDeclElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}else if (isEntityInst(type)){
			return new EntityInstElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}else if (isPort(type)){
			return new VhdlPortElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isGeneric(type)){
			return new GenericElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isSignal(type)){
			return new VhdlSignalElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isVariable(type)){
			return new VariableElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isConstant(type)){
			return new ConstantElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isAlias(type)){
			return new AliasElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isFile(type)){
			return new FileElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isRecordMember(type)){
			return new RecordMemberElement(name,type,startLine,startCol,endLine,endCol,file,true);
		}
		else if (isTypeDecl(type)){
            return new TypeDecl(name,type,startLine,startCol,endLine,endCol,file,true);
        }
		else if (isSubTypeDecl(type)){
			return new SubTypeDecl(name,type,startLine,startCol,endLine,endCol,file,true);
        }
		else if (isUseClause(type)){
			return new UseClauseElement(name,type,startLine,startCol,endLine,endCol,file,false);
        }
		
		//default case
		return new OutlineElement(name,type,startLine,startCol,endLine,endCol,file,true);
	}
}
