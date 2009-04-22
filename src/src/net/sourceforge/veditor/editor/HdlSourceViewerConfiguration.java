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

import java.util.Iterator;
import java.util.Vector;

import net.sourceforge.veditor.document.HdlDocument;
import net.sourceforge.veditor.document.VhdlDocument;
import net.sourceforge.veditor.editor.scanner.HdlCommentScanner;
import net.sourceforge.veditor.editor.scanner.HdlPartitionScanner;
import net.sourceforge.veditor.editor.scanner.HdlScanner;
import net.sourceforge.veditor.parser.OutlineElement;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.texteditor.MarkerAnnotation;

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
	
	private HdlScanner getHdlScanner()
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
		dr = new DefaultDamagerRepairer(getHdlScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		
	    String[] contentTypes = HdlPartitionScanner.getContentTypes();
		HdlTextAttribute[] attrs = HdlPartitionScanner.getContentTypeAttributes();
		for (int i = 0; i < contentTypes.length; i++)
		{
			addRepairer(reconciler, attrs[i], contentTypes[i]);
		}
		
	    addCommentScanner(reconciler, HdlTextAttribute.SINGLE_LINE_COMMENT,HdlPartitionScanner.SINGLE_LINE_COMMENT);
	    addCommentScanner(reconciler, HdlTextAttribute.MULTI_LINE_COMMENT,HdlPartitionScanner.MULTI_LINE_COMMENT);
	    addCommentScanner(reconciler, HdlTextAttribute.DOXYGEN_COMMENT,HdlPartitionScanner.DOXYGEN_SINGLE_LINE_COMMENT);     
	    addCommentScanner(reconciler, HdlTextAttribute.DOXYGEN_COMMENT,HdlPartitionScanner.DOXYGEN_MULTI_LINE_COMMENT);
        
		return reconciler;
	}
	
	private void addCommentScanner(PresentationReconciler reconciler ,HdlTextAttribute attr,String contentType){	

	    Token defaultToken=new Token(attr.getTextAttribute(colorManager));
	    HdlCommentScanner commentScanner=new HdlCommentScanner(colorManager,defaultToken);        
	    DefaultDamagerRepairer dr = new DefaultDamagerRepairer(commentScanner);
        reconciler.setDamager(dr, contentType);
        reconciler.setRepairer(dr, contentType);
    
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

	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer)
	{
		return new AnnotationHover();
	}
	
	public ITextHover getTextHover(ISourceViewer sourceViewer,
			String contentType)
	{
		return new TextHover();
	}

	private static class AnnotationHover implements IAnnotationHover
	{

		@SuppressWarnings("unchecked")
		public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber)
		{
			IAnnotationModel model = sourceViewer.getAnnotationModel();
			if (model == null)
				return null;
			
			// lineNumber starts from 0, not 1
			lineNumber++;

	
			Iterator i = model.getAnnotationIterator();;
			String messages = null;

			while(i.hasNext())
			{
				Object annotaion = i.next();
				if (annotaion instanceof MarkerAnnotation)
				{
					IMarker marker = ((MarkerAnnotation)annotaion).getMarker();
					int refline = marker.getAttribute(IMarker.LINE_NUMBER, 0);
					if (refline == lineNumber)
					{
						String mkmsg = marker.getAttribute(IMarker.MESSAGE, "");
						if (messages == null)
							messages = mkmsg;
						else
							messages += "\n" + mkmsg;
					}
				}
			}
			return messages;
		}
	}
	private static class TextHover implements ITextHover
	{
		public IRegion getHoverRegion(ITextViewer textViewer, int offset)
		{
			String text = textViewer.getDocument().get();
			int length = text.length();

			int begin = offset;
			while(begin >= 0 && isWordCharacter(text.charAt(begin)))
			{
				begin --;
			}
			begin++;
			
			int end = offset;
			while(end < length && isWordCharacter(text.charAt(end)))
			{
				end++;
			}
			
			if (end - begin > 0)
				return new Region(begin, end - begin);
			else
				return null;
		}
		
		public String getHoverInfo(ITextViewer textViewer, IRegion region)
		{
			String text;

			// FIXME: module and signal is contents dependent
			HdlDocument doc = (HdlDocument)textViewer.getDocument(); 
			try
			{
				text = doc.get(region.getOffset(), region.getLength());
			}
			catch (BadLocationException e)
			{
				return null;
			}

			String hover = getVariableHover(text, doc, region.getOffset());
			return hover;
		}

		private boolean isWordCharacter(char ch)
		{
			return Character.isJavaIdentifierPart(ch);
		}
		
		private String getVariableHover(String text, HdlDocument doc, int offset)
		{
			String results="";
			Vector<OutlineElement> definitions=doc.getDefinitionList(text, offset);				
			for(OutlineElement element:definitions){
				String commentString;
				results+=doc.getOutlineDatabase().getOutlineContainer(element.getFile()).getCommentsNear(element);
				if(doc instanceof VhdlDocument){
					commentString="--";
				}
				else{
					commentString="//";
				}
				results=results.trim();
				if(results.length() > 0){
					results=results.replaceAll("^",commentString+" ");
					results=results.replaceAll("\n","\n"+commentString+" ");
					results+="\n";
				}
				results+=element.getLongName()+"\n";
			}
			
			return results;
		}
	}
}








