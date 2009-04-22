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

package net.sourceforge.veditor.editor.scanner.verilog;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.veditor.editor.scanner.HdlPartitionScanner;

import org.eclipse.jface.text.rules.*;

public class VerilogPartitionScanner extends HdlPartitionScanner {
    public VerilogPartitionScanner() {
        super();

        List<Object> rules = new ArrayList<Object>();

        //rules.add(new VerilogCommentRule(taskTagToken));
        
        // doxygen comment
        rules.add(new EndOfLineRule("///", doxygenSingleLineCommentToken));
        rules.add(new EndOfLineRule("//@", doxygenSingleLineCommentToken));
        rules.add(new MultiLineRule("/**", "*/", doxygenSingleLineCommentToken));

        // single line comments.
        rules.add(new EndOfLineRule("//", singleLineCommentToken));

        // strings.
        rules.add(new SingleLineRule("\"", "\"", stringToken, '\\'));

        // special case word rule.
        EmptyCommentRule wordRule = new EmptyCommentRule(multiLineCommentToken);
        rules.add(wordRule);

        // multi-line comments
        rules.add(new MultiLineRule("/*", "*/", multiLineCommentToken));

        IPredicateRule[] result = new IPredicateRule[rules.size()];
        rules.toArray(result);
        setPredicateRules(result);
    }
    
    public int getOffset(){
        return fOffset;
    }

    /**
     * Word rule for empty comments.
     */
    private static class EmptyCommentRule extends WordRule implements
            IPredicateRule {
        private IToken successToken;

        public EmptyCommentRule(IToken successToken) {
            super(new EmptyCommentDetector());
            this.successToken = successToken;
            addWord("/**/", this.successToken);
        }

        public IToken evaluate(ICharacterScanner scanner, boolean resume) {
            return evaluate(scanner);
        }

        public IToken getSuccessToken() {
            return successToken;
        }

        /**
         * Detector for empty comments.
         */
        private static class EmptyCommentDetector implements IWordDetector {
            public boolean isWordStart(char c) {
                return (c == '/');
            }

            public boolean isWordPart(char c) {
                return (c == '*' || c == '/');
            }
        }

    }
}