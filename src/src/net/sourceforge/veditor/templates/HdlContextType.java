package net.sourceforge.veditor.templates;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

public class HdlContextType extends TemplateContextType{
	
	/** This context's id */
	public static final String CONTEXT_TYPE= "net.sourceforge.veditor.HdlContext"; 

	/**
	 * Creates a new HdlContextType context type. 
	 */
	public HdlContextType() {
		super();
		addGlobalResolvers();
	}

	protected void addGlobalResolvers() {		
		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.WordSelection());
		addResolver(new GlobalTemplateVariables.LineSelection());
		addResolver(new GlobalTemplateVariables.Dollar());
		addResolver(new GlobalTemplateVariables.Date());
		addResolver(new GlobalTemplateVariables.Year());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.User());
		//mg
		addResolver( new VhdlTemplateVariable.Brief() );
		//mg-------------------
	}
}
