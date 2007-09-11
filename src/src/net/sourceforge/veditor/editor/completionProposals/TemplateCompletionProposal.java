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


import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Point;

public class TemplateCompletionProposal extends CompletionProposal
{
	private int cursor;
	private String display;

	public TemplateCompletionProposal(
		String replace,
		int offset,
		int length,
		int cursor,
		String display)
	{
		super(replace, offset, length);
		this.cursor = cursor;
		this.display = display;
	}
	public String getDisplayString()
	{
		return display;
	}
	public Point getSelection(IDocument document)
	{
		return new Point(getOffset() - getLength() + cursor, 0);
	}
}

