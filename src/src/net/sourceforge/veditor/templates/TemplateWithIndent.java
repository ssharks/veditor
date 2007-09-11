package net.sourceforge.veditor.templates;

import org.eclipse.jface.text.templates.Template;

public class TemplateWithIndent extends Template
{
	private String indent;

	public TemplateWithIndent(Template parent, String indent)
	{
		super(parent);
		this.indent = indent;
	}
	
	public String getPattern()
	{
		StringBuffer pattern = new StringBuffer();
		int length = super.getPattern().length();
		for(int i = 0; i < length; i++)
		{
			char c = super.getPattern().charAt(i);
			pattern.append(c);
			if (c == '\n')
				pattern.append(indent);
		}
		return pattern.toString();
	}
}
