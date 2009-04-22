/*******************************************************************************
 * Copyright (c) 2009 VEditor Team
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ali G - initial API and implementation
 *****************************************************************************/

package net.sourceforge.veditor.editor.scanner;
import net.sourceforge.veditor.editor.ColorManager;
import net.sourceforge.veditor.editor.HdlTextAttribute;
import net.sourceforge.veditor.parser.IParser;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;


/**
 * Scanner to highlight meta-keywords inside of comments
 * @author gho18481
 *
 */
public class HdlCommentScanner extends RuleBasedScanner {
    /**
     * Class constructor
     * @param manager The color manager
     */
    public HdlCommentScanner(ColorManager manager,IToken defaultToken){        
        IToken autoTask = new Token(HdlTextAttribute.AUTOTASKS.getTextAttribute(manager));        
        
        WordRule wordRule=new WordRule(new WordDetector());
        
        for(String autoTaskKeyWord:IParser.taskCommentTokens){
            wordRule.addWord(autoTaskKeyWord, autoTask);
        }
        
        IRule [] rules=new IRule[1];
        rules[0]=wordRule;
        setRules(rules);
        setDefaultReturnToken(defaultToken);
    }
    
    /**
     * Private class to configure word boundaries
     * @author gho18481
     *
     */
    private class WordDetector implements IWordDetector{        

        public WordDetector() {        
        }

        public boolean isWordPart(char character) {
            return !Character.isWhitespace(character);
        }

        public boolean isWordStart(char character) {        
            return !Character.isWhitespace(character);
        }    
    }
}
