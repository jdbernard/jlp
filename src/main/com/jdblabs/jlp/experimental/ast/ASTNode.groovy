package com.jdblabs.jlp.experimental.ast

public class ASTNode {

    protected int lineNumber

    public ASTNode(int lineNum) { this.lineNumber = lineNum }

    public int getLineNumber() { lineNumber }
}
