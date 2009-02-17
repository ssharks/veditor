/*******************************************************************************
 * Copyright (c) 2004, 2006 KOBAYASHI Tadashi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Ali Ghorashi - initial implementation
 *******************************************************************************/
package net.sourceforge.veditor.actions;

import net.sourceforge.veditor.editor.VerilogEditor;
import net.sourceforge.veditor.editor.VhdlEditor;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;

import java.util.regex.*;


/**
 * find module declaration from project tree<p>
 * file name and module name must be same
 */
public class CommentAction extends AbstractAction
{
	public CommentAction()
	{
		super("Comment");
	}
	public void run()
	{
		StyledText widget = getViewer().getTextWidget();
		Point point = widget.getSelection();
		int begin = point.x;
		int end = point.y;
		String commentString="//";
		
		//get some vitals on the selection
		int StartingLine = widget.getLineAtOffset(begin);		
		begin = widget.getOffsetAtLine(StartingLine);
		
		String region = widget.getTextRange(begin, end-begin);
				
		//are we using VHDL or verilog
		if(getEditor() instanceof VhdlEditor){
			commentString="--";
		}
		else if (getEditor() instanceof VerilogEditor){
			commentString="//";
		}
		else{
			Error e=new Error("Unkown file type");
			throw e;
		}
		
		//some fancy foot work to replace the beginning of each line with the comment character
		region = Pattern.compile("^",Pattern.MULTILINE).matcher(region).replaceAll(commentString);
		
		//replace the selection
		widget.replaceTextRange(begin, end - begin, region.toString());
	}
}



