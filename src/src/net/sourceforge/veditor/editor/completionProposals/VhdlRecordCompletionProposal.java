package net.sourceforge.veditor.editor.completionProposals;

import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import net.sourceforge.veditor.VerilogPlugin;

public class VhdlRecordCompletionProposal extends CompletionProposal {
	private int cursor;
	private String display;

	public VhdlRecordCompletionProposal(String replace, int offset, int length,
			int cursor, String display) {
		super(replace, offset, length);
		this.cursor = cursor;
		this.display = display;
	}

	public String getDisplayString() {
		return display;
	}

	public Point getSelection(IDocument document) {
		return new Point(getOffset() - getLength() + cursor, 0);
	}

	public Image getImage() {
		return VerilogPlugin.getPlugin().getImage("$nl$/icons/obj.gif");
	}

}
