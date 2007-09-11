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

import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.editor.completionProposals.CompletionProposal;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;

public abstract  class InstanceCompletionProposal extends	CompletionProposal
{
	protected IProject proj;
	protected String name;
	protected String indentString;
	
	/**
	 * Class constructor 
	 * @param doc Document where the proposal is being made
	 * @param name Name of the proposal
	 * @param offset Offset of replace string from the beginning of the document
	 * @param length Length of the replace string
	 */
	public InstanceCompletionProposal(
		HdlDocument doc,
		String name,
		int offset,
		int length)
	{
		super("", offset, length);
		this.proj = doc.getProject();
		this.name = name;
		this.indentString = doc.getIndentString(offset);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.veditor.editor.HdlCompletionProcessor.CompletionProposal#apply(org.eclipse.jface.text.IDocument)
	 */
	abstract public void apply(IDocument document);
	
	/**
	 * @return The string used to display this proposal
	 */
	public String getDisplayString(){ return name; }
	/**
	 * @return The string containing the space characters where the proposal is being made
	 * All inserted lines should start with this string
	 */
	public String getIndentString(){ return indentString; }
}