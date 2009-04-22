package net.sourceforge.veditor.editor.scanner.verilog;

import org.eclipse.jface.text.rules.IWordDetector;

public class WordDetector implements IWordDetector{        

    public WordDetector() {
    }

    public boolean isWordPart(char character) {
        return Character.isJavaIdentifierPart(character);
    }

    public boolean isWordStart(char character) {
        if (character == '`')
            return true;
        return Character.isJavaIdentifierStart(character);
    }    
}
