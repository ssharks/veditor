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
import java.util.List;

import net.sourceforge.veditor.parser.Module;
import net.sourceforge.veditor.parser.ModuleList;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class VhdlCompletionProcessor extends HdlCompletionProcessor
{
	public List getInModuleProposals(HdlDocument doc, int offset, String replace)
	{
		int length = replace.length();
		List matchList = new ArrayList();

		//  template
		if (isMatch(replace, "process"))
			matchList.add(createProcess(doc, offset, length));

		//  module instantiation
		ModuleList mlist = ModuleList.find(doc.getProject());
		String[] mnames = mlist.getModuleNames();
		for (int i = 0; i < mnames.length; i++)
		{
			if (isMatch(replace, mnames[i]))
			{
				matchList.add(new VhdlInstanceCompletionProposal(doc,
						mnames[i], offset, length));
			}
		}

		//  reserved word
		String[] rwords = {"architecture ", "constant ", "signal "};
		for(int i = 0 ; i < rwords.length ; i++)
		{
			if (isMatch(replace, rwords[i]))
				matchList.add(getSimpleProposal(rwords[i], offset, length));
		}

		return matchList;
	}

	public List getInStatmentProposals(HdlDocument doc, int offset, String replace, String mname)
	{
		List matchList = new ArrayList();

		//  variable
		ModuleList mlist = ModuleList.find(doc.getProject());
		// FIX!!! should we do something more advanced when there
		// are no modules?
		if (mlist!=null)
		{
			Module module = mlist.findModule(mname);
			if (module != null)
			{
				addProposals(matchList, offset, replace, module.getPorts());
				addProposals(matchList, offset, replace, module.getVariables());
			}
		}
		return matchList;
	}
	
	private ICompletionProposal createProcess(IDocument doc, int offset, int length)
	{
		String indent = getIndent(doc, offset);
		String first = "process(";
		String second = ")\n" + indent + "begin\n" + indent + "\t\n" + indent
				+ "end process;\n";
		return getCompletionProposal(first + second, offset, length, first
				.length(), "process");
	}

	private class VhdlInstanceCompletionProposal extends
			InstanceCompletionProposal 
	{
		public VhdlInstanceCompletionProposal(
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
			StringBuffer replace = new StringBuffer(name + " : entity " + name);
			replace.append(indent + "\t");
			replace.append("port map(");

			Object[] ports = module.getPorts();
			for (int i = 0; i < ports.length; i++)
			{
				replace.append(indent + "\t\t");

				String port = ports[i].toString();
				replace.append( port + " => " + port );
				if (i < ports.length - 1)
					replace.append(", ");
			}
			replace.append(");");
			return replace.toString();
		}
	}
}




