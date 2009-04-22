package net.sourceforge.veditor.editor.scanner.vhdl;

import org.eclipse.jface.text.rules.IWordDetector;

public class WordDetector implements IWordDetector{        

    public WordDetector() {        
    }

    public boolean isWordPart(char character) {
        return Character.isJavaIdentifierPart(character);
    }

    public boolean isWordStart(char character) {        
        return Character.isJavaIdentifierStart(character);
    }    
}
