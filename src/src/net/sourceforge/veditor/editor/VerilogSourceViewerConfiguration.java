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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.RGB;

/**
 * ソースコードのコメントその他の構造を解析する
 */
public class VerilogSourceViewerConfiguration extends SourceViewerConfiguration
{
	private VerilogScanner scanner;
	private ColorManager colorManager;

	public VerilogSourceViewerConfiguration(ColorManager colorManager)
	{
		this.colorManager = colorManager;
	}
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
	{
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			VerilogPartitionScanner.VERILOG_SINGLE_LINE_COMMENT,
			VerilogPartitionScanner.VERILOG_MULTI_LINE_COMMENT,
			VerilogPartitionScanner.VERILOG_STRING };
	}

	protected VerilogScanner getVerilogScanner()
	{
		if (scanner == null)
		{
			scanner = new VerilogScanner(colorManager);
			scanner.setDefaultReturnToken(
				new Token(new TextAttribute(colorManager.getColor(VerilogColorConstants.DEFAULT))));
		}
		return scanner;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
	{
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr;

		dr = new DefaultDamagerRepairer(getVerilogScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		NonRuleBasedDamagerRepairer ndr;
		addRepairer(
			reconciler,
			VerilogColorConstants.SINGLE_LINE_COMMENT,
			VerilogPartitionScanner.VERILOG_SINGLE_LINE_COMMENT);

		addRepairer(
			reconciler,
			VerilogColorConstants.MULTI_LINE_COMMENT,
			VerilogPartitionScanner.VERILOG_MULTI_LINE_COMMENT);

		addRepairer(
			reconciler,
			VerilogColorConstants.STRING,
			VerilogPartitionScanner.VERILOG_STRING);

		return reconciler;
	}

	private void addRepairer(PresentationReconciler reconciler, RGB color, String partition)
	{
		NonRuleBasedDamagerRepairer ndr;
		ndr = new NonRuleBasedDamagerRepairer(new TextAttribute(colorManager.getColor(color)));
		reconciler.setDamager(ndr, partition);
		reconciler.setRepairer(ndr, partition);
	}

	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
	{

		ContentAssistant assistant = new ContentAssistant();
		assistant.setContentAssistProcessor(
			new VerilogCompletionProcessor(),
			IDocument.DEFAULT_CONTENT_TYPE);

		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);

		return assistant;
	}
}

