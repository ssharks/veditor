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

package net.sourceforge.veditor.parser;


/**
 * Segmentation for ContentOutline
 */
public abstract class Segment
{
	private int line ;
	private int length ;

	protected Segment(int line)
	{
		this.line = line;
		length = 1;
	}

//	public void setLine(int line)
//	{
//		this.line = line;
//		length = 1;
//	}
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
		return null ;
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
