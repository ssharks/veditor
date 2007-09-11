package net.sourceforge.veditor.editor.completionProposals;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.parser.OutlineElement;
import net.sourceforge.veditor.templates.TemplateWithIndent;
import net.sourceforge.veditor.templates.VhdlGlobalContext;

import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.graphics.Image;

public abstract class DynamicProposalProvider {
	HdlDocument m_Doc;
	OutlineElement m_Element;
	int m_Offset;
	int m_Length;
	
	/**
	 * Constructor
	 * @param doc
	 * @param element
	 * @param offset
	 * @param length
	 */
	protected DynamicProposalProvider(HdlDocument doc,
			OutlineElement element, int offset, int length){
		m_Doc=doc;
		m_Element=element;
		//move the offset to the beginning of of the replaced string 
		m_Offset=offset-length;
		//length of the string being replace
		m_Length=length;
	}
	
	/**
	 * This function creates a new proposal
	 * @return
	 */
	public HdlTemplateProposal createProposal() {
		Region region = null;
		Image image;
		int relevance = 0;
		TemplateWithIndent templateX = null;
		DocumentTemplateContext context = null;
		Template template = null;

		image = VerilogPlugin.getPlugin().getImage(m_Element.GetImageName());
		context = new DocumentTemplateContext(new VhdlGlobalContext(), m_Doc,
				m_Offset, m_Length);
		//set the region to cover the text begin replaced
		region = new Region(m_Offset, m_Length);

		template = new Template(
				m_Element.getShortName(), 
			    getDescription(),
			    VhdlGlobalContext.CONTEXT_TYPE,
				getString(),
				false);
		
		templateX = new TemplateWithIndent(template, m_Doc.getIndentString(m_Offset));

		return new HdlTemplateProposal(
				templateX, 
				context, 
				region, 
				image,
				relevance);

	}
	/**
	 * Returns a string used for completing this proposal
	 * @return
	 */
	protected abstract String getString();
	/**
	 * Returns a string describing this proposal
	 * @return
	 */
	protected abstract String getDescription();
	
}
