//
//  Copyright 2004, KOBAYASHI Tadashi
//  $Id$
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package net.sourceforge.veditor.editor;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.veditor.parser.Module;
import net.sourceforge.veditor.parser.ModuleList;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class VerilogCompletionProcessor extends HdlCompletionProcessor
{
	public List getInModuleProposals(HdlDocument doc, int offset,
			String replace)
	{
		int length = replace.length();
		List matchList = new ArrayList();

		//  template
		if (isMatch(replace, "always"))
			matchList.add(createAlways(doc, offset, length));
		if (isMatch(replace, "initial"))
			matchList.add(createInitial(doc, offset, length));
		if (isMatch(replace, "function"))
			matchList.add(createFunction(doc, offset, length));
		if (isMatch(replace, "task"))
			matchList.add(createTask(doc, offset, length));

		//  reserved word
		String[] rwords = {"assign ", "integer ", "parameter "};
		for(int i = 0 ; i < rwords.length ; i++)
		{
			if (isMatch(replace, rwords[i]))
				matchList.add(getSimpleProposal(rwords[i], offset, length));
		}

		//  module instantiation
		ModuleList mlist = ModuleList.find(doc.getProject());
		String[] mnames = mlist.getModuleNames();
		for (int i = 0; i < mnames.length; i++)
		{
			if (isMatch(replace, mnames[i]))
			{
				matchList.add(new VerilogInstanceCompletionProposal(doc,
						mnames[i], offset, length));
			}
		}
		return matchList;
	}

	public List getInStatmentProposals(
		HdlDocument doc,
		int offset,
		String replace,
		String mname)
	{
		List matchList = new ArrayList();

		//  template
		if (isMatch(replace, "begin"))
			matchList.add(createBeginEnd(doc, offset, replace.length()));

		//  variable
		ModuleList mlist = ModuleList.find(doc.getProject());
		Module module = mlist.findModule(mname);
		if (module != null)
		{
			addProposals(matchList, offset, replace, module.getPorts());
			addProposals(matchList, offset, replace, module.getVariables());
		}
		return matchList;
	}

	//  code templates
	private ICompletionProposal createBeginEnd(IDocument doc, int offset, int length)
	{
		String indent = getIndent(doc, offset);
		String str = "begin\n" + indent + "\t\n" + indent + "end";
		int cursor = 7 + indent.length();
		return getCompletionProposal(str, offset, length, cursor, "begin/end");
	}
	private ICompletionProposal createAlways(IDocument doc, int offset, int length)
	{
		String first = "always @(posedge clk) begin\n\t";
		String second = "\nend\n";
		return getCompletionProposal(first + second, offset, length, first.length(), "always");
	}
	private ICompletionProposal createInitial(IDocument doc, int offset, int length)
	{
		String first = "initial begin\n\t";
		String second = "\nend\n";
		return getCompletionProposal(first + second, offset, length, first.length(), "initial");
	}
	private ICompletionProposal createFunction(IDocument doc, int offset, int length)
	{
		String first = "function ";
		String second = ";\nbegin\n\t\nend\nendfunction\n";
		return getCompletionProposal(first + second, offset, length, first.length(), "function");
	}
	private ICompletionProposal createTask(IDocument doc, int offset, int length)
	{
		String first = "task ";
		String second = ";\nbegin\n\t\nend\nendtask\n";
		return getCompletionProposal(first + second, offset, length, first.length(), "task");
	}

	private class VerilogInstanceCompletionProposal extends
			InstanceCompletionProposal 
	{
		public VerilogInstanceCompletionProposal(
			HdlDocument doc,
			String name,
			int offset,
			int length)
		{
			super(doc, name, offset, length);
		}

		public String getReplaceString(Module module)
		{
			String name = module.toString();
			String indent = "\n" + getIndentString();
			StringBuffer replace = new StringBuffer(name + " " + name + "(");
			Object[] ports = module.getPorts();
			for (int i = 0; i < ports.length; i++)
			{
				replace.append(indent + "\t");

				String port = ports[i].toString();
				replace.append("." + port + "(" + port + ")");

				if (i < ports.length - 1)
					replace.append(",");
			}
			replace.append(indent + ");");
			return replace.toString();
		}
	}
}


