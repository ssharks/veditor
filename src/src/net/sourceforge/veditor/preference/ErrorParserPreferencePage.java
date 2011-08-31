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

	private java.util.List<ErrorParser> parserList;
	private List compilerList;
	Button newButton;
	Button removeButton;
	private Text errText;
	private Button m_ErrorPatternButton;
	private Text warnText;
	private Button m_WarnPatternButton;
	private Text infoText;
	private Button m_InfoPatternButton;
	
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
		field.setLayout(new GridLayout(3, false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		field.setLayoutData(gd);
		
		errText = createText(field, "Error Pattern:");
		m_ErrorPatternButton=new Button(field,SWT.PUSH);
		m_ErrorPatternButton.setText("...");
		m_ErrorPatternButton.addSelectionListener(new PatternBuilderListener(errText));
		warnText = createText(field, "Warning Pattern:");
		m_WarnPatternButton=new Button(field,SWT.PUSH);
		m_WarnPatternButton.setText("...");
		m_WarnPatternButton.addSelectionListener(new PatternBuilderListener(warnText));
		infoText = createText(field, "Info Pattern:");
		m_InfoPatternButton=new Button(field,SWT.PUSH);
		m_InfoPatternButton.setText("...");
		m_InfoPatternButton.addSelectionListener(new PatternBuilderListener(infoText));
		
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
		
			boolean editable = ((ErrorParser)parserList.get(idx)).isEditable();
			errText.setEditable(editable);
			warnText.setEditable(editable);
			infoText.setEditable(editable);
			m_ErrorPatternButton.setEnabled(editable);
			m_WarnPatternButton.setEnabled(editable);
			m_InfoPatternButton.setEnabled(editable);
		}
		else
		{
			errText.setText("");
			warnText.setText("");
			infoText.setText("");
			errText.setEditable(false);
			warnText.setEditable(false);
			infoText.setEditable(false);
			m_ErrorPatternButton.setEnabled(false);
			m_WarnPatternButton.setEnabled(false);
			m_InfoPatternButton.setEnabled(false);
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
    
    /**
	 * Class called when the user presses pattern builder button
	 *
	 */
	private class PatternBuilderListener extends SelectionAdapter {
		Text m_TargetTextBox;

		/**
		 * Class constructor
		 * 
		 * @param text  The text box we need to fill the data with
		 */
		public PatternBuilderListener(Text text) {
			super();
			m_TargetTextBox = text;
		}

		public void widgetSelected(SelectionEvent e) {

			PatternBuilderDialog dialog = new PatternBuilderDialog(getControl()
					.getShell(),m_TargetTextBox.getText());
			dialog.open();
			m_TargetTextBox.setText(dialog.getPattern());
			
		}
	}
}

