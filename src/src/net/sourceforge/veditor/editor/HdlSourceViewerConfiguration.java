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
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

/**
 * parse verilog source code
 */
abstract public class HdlSourceViewerConfiguration extends
		SourceViewerConfiguration
{
	private HdlScanner scanner;
	private ColorManager colorManager;

	public static HdlSourceViewerConfiguration createForVerilog(
			ColorManager colorManager)
	{
		return new HdlSourceViewerConfiguration(colorManager)
		{
			public HdlScanner createScanner()
			{
				return HdlScanner.createForVerilog(getColorManager());
			}
			public HdlCompletionProcessor createCompletionProcessor()
			{
				return new VerilogCompletionProcessor();
			}
		};
	}
	public static HdlSourceViewerConfiguration createForVhdl(
			ColorManager colorManager)
	{
		return new HdlSourceViewerConfiguration(colorManager)
		{
			public HdlScanner createScanner()
			{
				return HdlScanner.createForVhdl(getColorManager());
			}
			public HdlCompletionProcessor createCompletionProcessor()
			{
				return new VhdlCompletionProcessor();
			}
		};
	}
	
	public HdlSourceViewerConfiguration(ColorManager colorManager)
	{
		this.colorManager = colorManager;
	}

	abstract HdlScanner createScanner();
	abstract HdlCompletionProcessor createCompletionProcessor();
	
	public ColorManager getColorManager()
	{
		return colorManager;
	}

	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
	{
		String[] types = HdlPartitionScanner.getContentTypes();
		String[] ret = new String[types.length+1];
		ret[0] = IDocument.DEFAULT_CONTENT_TYPE; 
		for( int i = 0 ; i < types.length ; i++ )
			ret[i+1] = types[i];
		return ret;
	}
	
	private HdlScanner getVerilogScanner()
	{
		if (scanner == null)
		{
			scanner = createScanner();
			scanner.setDefaultReturnToken(new Token(
					HdlTextAttribute.DEFAULT.getTextAttribute(colorManager)));
		}
		return scanner;
	}

	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer)
	{
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr;
		dr = new DefaultDamagerRepairer(getVerilogScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		String[] contentTypes = HdlPartitionScanner.getContentTypes();
		HdlTextAttribute[] attrs = HdlPartitionScanner.getContentTypeAttributes();
		for (int i = 0; i < contentTypes.length; i++)
		{
			addRepairer(reconciler, attrs[i], contentTypes[i]);
		}
		return reconciler;
	}

	private void addRepairer(PresentationReconciler reconciler,
			HdlTextAttribute attr, String partition)
	{
		NonRuleBasedDamagerRepairer ndr;
		ndr = new NonRuleBasedDamagerRepairer(attr
				.getTextAttribute(colorManager));
		reconciler.setDamager(ndr, partition);
		reconciler.setRepairer(ndr, partition);
	}

	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
	{
		ContentAssistant assistant = new ContentAssistant();
		assistant.setContentAssistProcessor(createCompletionProcessor(),
				IDocument.DEFAULT_CONTENT_TYPE);

		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant
				.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant
				.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);

		return assistant;
	}
}



