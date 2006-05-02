//
//  Copyright 2004, 2006 KOBAYASHI Tadashi
//  $Id$
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package net.sourceforge.veditor.preference;

import net.sourceforge.veditor.VerilogPreferenceInitializer;
import net.sourceforge.veditor.builder.ErrorParser;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

public class ErrorParserPreferencePage extends AbstractPreferencePage
{
	private static final int NUM_OF_DEFAULT_ERROR_PARSERS = VerilogPreferenceInitializer.NUM_OF_DEFAULT_ERROR_PARSERS;

	private java.util.List parserList;
	private List compilerList;
	Button newButton;
	Button removeButton;
	private Text errText;
	private Text warnText;
	private Text infoText;
	
	protected Control createContents(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		createSection(composite);
		createTextField(composite);
		initializeSelection();

		return composite;
	}
	
	private void createSection(Composite parent)
	{
		// create compiler selection
		Composite field = new Composite(parent, SWT.NONE);
		field.setLayout(new GridLayout(2, false));
		compilerList = new List(field, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		GridData listgd = new GridData();
		listgd.heightHint = 80;
		compilerList.setLayoutData(listgd);

		compilerList.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int idx = compilerList.getSelectionIndex();
				changeSelection(idx);
			}
		});
		
		createButtons(field);
	}

	private void createButtons(Composite field)
	{
		// create new/remove button
		Composite buttonField = new Composite(field, SWT.NONE);
		buttonField.setLayout(new GridLayout(1, false));
		newButton = new Button(buttonField, SWT.PUSH);
		removeButton = new Button(buttonField, SWT.PUSH);
		newButton.setText("&New...");
		removeButton.setText("&Remove");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		newButton.setLayoutData(gd);
		removeButton.setLayoutData(gd);
		
		newButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				InputDialog dlog = new InputDialog(getShell(),
						"New Error Parser", "Compiler name:", "",
						new NameValidator());
				dlog.open();
				String name = dlog.getValue();
				if (name != null && name.length() > 0)
				{
					addParser(name);
				}
			}
		});
		
		removeButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int idx = compilerList.getSelectionIndex();
				if (idx >= NUM_OF_DEFAULT_ERROR_PARSERS
						&& idx < parserList.size())
				{
					parserList.remove(idx);
					updateSelection();

					if (idx >= parserList.size())
						idx = parserList.size() - 1;
					changeSelection(idx);
				}
			}
		});
	}
	
	private void createTextField(Composite parent)
	{
		Composite field = new Composite(parent, SWT.NONE);
		field.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		field.setLayoutData(gd);
		
		errText = createText(field, "Error Pattern:");
		warnText = createText(field, "Warning Pattern:");
		infoText = createText(field, "Info Pattern:");
		
		errText.addModifyListener(new TextModifyListener(errText,0));
		warnText.addModifyListener(new TextModifyListener(warnText,1));
		infoText.addModifyListener(new TextModifyListener(infoText,2));
	}
	
	private Text createText(Composite parent, String name)
	{
		Label label = new Label(parent, SWT.NULL);
		label.setText(name);
		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(gd);

		return text;
	}

	private void initializeSelection()
	{
		parserList = ErrorParser.getParserList();

		updateSelection();
		changeSelection(0);
	}
	
	/**
	 * add new parser
	 * @param newCompiler	compiler name
	 */
	private void addParser(String newCompiler)
	{
		ErrorParser newParser = new ErrorParser(newCompiler);
		newParser.setRegex("", "", "");
		parserList.add(newParser);
		updateSelection();

		changeSelection(parserList.size() - 1);
	}

	/**
	 * It is called when the parser list is modified
	 */
	private void updateSelection()
	{
		String labels[] = new String[parserList.size()];
		for (int i = 0; i < parserList.size(); i++)
		{
			labels[i] = ((ErrorParser)parserList.get(i)).getCompilerName();
		}
		compilerList.setItems(labels);
	}
	
	/**
	 * It is called when the selection focus is changed
	 */
	private void changeSelection(int idx)
	{
		if (0 <= idx && idx < parserList.size())
		{
			compilerList.select(idx);
			boolean removable = (idx >= NUM_OF_DEFAULT_ERROR_PARSERS);
			removeButton.setEnabled(removable);
		
			updateTextField(idx);
		}
		else
		{
			// maybe never executed
			removeButton.setEnabled(false);
			updateTextField(-1);
		}
	}
	
	private void updateTextField(int idx)
	{
		if (idx >= 0)
		{
			ErrorParser parser = (ErrorParser)parserList.get(idx);
			errText.setText(parser.getErrorRegex());
			warnText.setText(parser.getWarningRegex());
			infoText.setText(parser.getInfoRegex());
		
			boolean editable = (idx >= NUM_OF_DEFAULT_ERROR_PARSERS);
			errText.setEditable(editable);
			warnText.setEditable(editable);
			infoText.setEditable(editable);
		}
		else
		{
			errText.setText("");
			warnText.setText("");
			infoText.setText("");
			errText.setEditable(false);
			warnText.setEditable(false);
			infoText.setEditable(false);
		}
	}

	/**
	 * for new compiler dialog
	 */
	private static class NameValidator implements IInputValidator
	{
		public String isValid(String newText)
		{
			if (newText.length() == 0)
			{
				return "Compiler name must be specified";
			}
			return null;
		}
	}
	
	/**
	 * it is called when errText, warnText or infoText is modified
	 */
	private class TextModifyListener implements ModifyListener
	{
		private Text text;
		private int mode;

		public TextModifyListener(Text text, int mode)
		{
			this.text = text;
			this.mode = mode;
		}
		public void modifyText(ModifyEvent e)
		{
			int idx = compilerList.getSelectionIndex();
			ErrorParser parser = (ErrorParser)parserList.get(idx);
			parser.setRegex(mode, text.getText());
		}
	}

	public boolean performOk()
	{
		super.performOk();
		ErrorParser.setParserList(parserList);
		return true;
	}

    protected void performDefaults()
    {
    	super.performDefaults();
    	ErrorParser.setDefaultParsers();
    	initializeSelection();
    }
}

