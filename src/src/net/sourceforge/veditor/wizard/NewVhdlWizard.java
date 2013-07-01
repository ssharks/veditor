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

package net.sourceforge.veditor.wizard;

import java.util.Calendar;

public class NewVhdlWizard extends NewHdlWizard
{
	public void addPages()
	{
		super.addPages(".vhd");
	}

	String getInitialContents( String templatePattern,  String moduleName,  String brief ) {
		String results;	


		results = templatePattern;
		results = results.replaceAll( "\\$\\{brief\\}", brief );
		
		results=results.replaceAll("\\$\\{modulename\\}", moduleName);
		results=results.replaceAll("\\$\\{user\\}", System.getProperty("user.name"));
		results=results.replaceAll("\\$\\{year\\}", Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
		String month = Integer.toString(Calendar.getInstance().get(Calendar.MONTH)+1);
		if(month.length()<2) month = "0"+month;
		String day = Integer.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		if(day.length()<2) day = "0"+day;
		results=results.replaceAll("\\$\\{month\\}", month);
		results=results.replaceAll("\\$\\{day\\}", day);

		return results.toString();
	}
	

}

