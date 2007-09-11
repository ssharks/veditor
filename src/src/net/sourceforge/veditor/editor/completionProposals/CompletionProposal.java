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
package net.sourceforge.veditor.editor.completionProposals;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class CompletionProposal implements IComparableCompletionProposal
{
	private String replace;
	private int offset, length;

	public CompletionProposal(String replace, int offset, int length)
	{
		this.replace = replace;
		this.offset = offset;
		this.length = length;
	}
	public void apply(IDocument document)
	{
		try
		{
			document.replace(offset - length, length, replace.toString());
		}
		catch (BadLocationException e)
		{
		}
	}
	public Point getSelection(IDocument document)
	{
		return null;
	}
	public String getAdditionalProposalInfo()
	{
		return null;
	}
	public Image getImage()
	{
		return null;
	}
	public IContextInformation getContextInformation()
	{
		return null;
	}
	public String toString()
	{
		return getDisplayString();
	}
	public int compareTo(Object arg)
	{
		return toString().compareTo(arg.toString());
	}
	public String getDisplayString()
	{
		return replace;
	}
	public int getLength()
	{
		return length;
	}
	public int getOffset()
	{
		return offset;
	}
}