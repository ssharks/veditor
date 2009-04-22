/*******************************************************************************
 * Copyright (c) 2009 VEditor Team
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ali G - initial API and implementation
 *******************************************************************************/
package net.sourceforge.veditor.editor.scanner.vhdl;

import java.util.ArrayList;
import java.util.List;


import net.sourceforge.veditor.editor.scanner.HdlPartitionScanner;

import org.eclipse.jface.text.rules.*;

/**
 * This class breaks up a VHDL document into partitions
 * @author gho18481
 *
 */
public class VhdlPartitionScanner extends HdlPartitionScanner {
    public VhdlPartitionScanner() {
        super();

        List<IPredicateRule> rules = new ArrayList<IPredicateRule>();

        rules.add(new VhdlCommentRule(taskTagToken));

        // doxygen comment
        //rules.add(new EndOfLineRule("--!", doxygenCommentToken));

        // single line comments.
        //rules.add(new EndOfLineRule("--", singleLineCommentToken));

        // strings.
        rules.add(new SingleLineRule("\"", "\"", stringToken, '\\'));
        rules.add(new WordPatternRule(new StdLogicDetector(), "\'", "\'",
                stringToken));

        IPredicateRule[] result = new IPredicateRule[rules.size()];
        rules.toArray(result);
        setPredicateRules(result);
    }

    private static class StdLogicDetector implements IWordDetector {
        public boolean isWordStart(char c) {
            return (c == '\'');
        }

        public boolean isWordPart(char c) {
            String words = "UX01ZWLH-";
            return (c == '\'') || (words.indexOf(c) != -1);
        }
    }   
    
    public int getOffset(){
        return fOffset;
    }

}
