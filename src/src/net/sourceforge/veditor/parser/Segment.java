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

package net.sourceforge.veditor.parser;


/**
 * Segmentation for ContentOutline
 */
public abstract class Segment
{
	private int line;
	private int length;

	protected Segment()
	{
		this(-1);
	}
	protected Segment(int line)
	{
		setLine(line);
	}
	public void setLine(int line)
	{
		this.line = line;
		length = 1;
	}

//	public void setLine(int begin, int end)
//	{
//		setLine(begin);
//		setEndLine(end);
//	}

	public void setEndLine(int line)
	{
		length = line - this.line + 1;
	}

	public Segment getParent()
	{
		return null;
	}

	public int getLine()
	{
		return line;
	}
	public int getLength()
	{
		return length;
	}
}



