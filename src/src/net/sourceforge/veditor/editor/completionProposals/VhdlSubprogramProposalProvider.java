package net.sourceforge.veditor.editor.completionProposals;

import net.sourceforge.veditor.VerilogPlugin;
import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.parser.vhdl.VhdlOutlineElementFactory.VhdlSubprogram;

public class VhdlSubprogramProposalProvider extends DynamicProposalProvider {
	private final static int MAX_LENGTH=80;
	
	public VhdlSubprogramProposalProvider(HdlDocument doc,
			VhdlSubprogram element, int offset, int length){
		super(doc,element,offset,length);
	}

	@Override
	protected String getString(){		
		//attempt to get a short string
		String results=getStringShort();
		//if the results is too long, use the tall version
		if(results.length() > MAX_LENGTH){
			return getStringTall();
		}
		return results;
	}
	
	private String getStringShort(){
		VhdlSubprogram sub = (VhdlSubprogram) m_Element;

		StringBuffer buff = new StringBuffer(sub.getShortName());
		String replaceString;
		VhdlSubprogram.Parameter[] params = sub.getParameters();
		int lastComma = 0;

		if (params.length > 0) {
			buff.append("(");
			for (VhdlSubprogram.Parameter param : params) {				
				buff.append(param.m_Name);
				buff.append(" => ${");
				buff.append(param.m_Name+"_"+param.m_Direction);
				buff.append("}");
				lastComma = buff.length();
				buff.append(", ");				
			}
			// erase the last comma
			buff.replace(lastComma, lastComma + 1, " ");
			buff.append(")");
		}

		buff.append(";");
		replaceString = buff.toString();
		return replaceString;
	}
	
	private String getStringTall() {
		VhdlSubprogram sub = (VhdlSubprogram) m_Element;

		StringBuffer buff = new StringBuffer(sub.getShortName());
		String replaceString;
		VhdlSubprogram.Parameter[] params = sub.getParameters();
		int lastComma = 0;

		if (params.length > 0) {
			buff.append("(\n");
			for (VhdlSubprogram.Parameter param : params) {
				buff.append("    ");
				buff.append(param.m_Name);
				buff.append(" => ${");
				buff.append(param.m_Name);
				buff.append("}");
				lastComma = buff.length();
				buff.append(", --");
				buff.append(param.m_Direction);
				buff.append("\n");
			}
			// erase the last comma
			buff.replace(lastComma, lastComma + 1, " ");
			buff.append(")");
		}

		buff.append(";");
		replaceString = buff.toString();
		// align the string
		replaceString = VerilogPlugin.alignOnChar(replaceString, '=', 1);
		replaceString = VerilogPlugin.alignOnChar(replaceString, ',', 1);
		replaceString = VerilogPlugin.alignOnChar(replaceString, '-', 1);
		// add the indent string
		return replaceString;
	}
	
	@Override
	protected String getDescription() {
		return m_Element.getLongName();
	}

}
