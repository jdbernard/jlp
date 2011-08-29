package com.jdblabs.jlp.ast

import java.util.ArrayList
import java.util.List

public class DocBlock implements ASTNode {
    
    public final int lineNumber
    public List<Directive> directives = new ArrayList<Directive>()
    public List<TextBlock> textBlocks = new ArrayList<TextBlock>()

    public DocBlock(int lineNumber) { this.lineNumber = lineNumber }

    public int getLineNumber() { lineNumber }

    public String toString() {
        "[DocBlock: Directives ${directives}, TextBlocks ${textBlocks}]" }
}
