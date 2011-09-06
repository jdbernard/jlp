package com.jdblabs.jlp.ast

import java.util.ArrayList
import java.util.List

public class DocBlock extends ASTNode {
    
    public List<Directive> directives = new ArrayList<Directive>()
    public List<DocText> docTexts = new ArrayList<DocText>()

    public DocBlock(int lineNumber) { super(lineNumber) }

    public String toString() {
        "[${lineNumber}:DocBlock: Directives ${directives}, DocTexts ${docTexts}]" }
}
