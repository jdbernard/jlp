package com.jdblabs.jlp.experimental.ast

public class DocText extends ASTNode {

    public String value

    public DocText(int lineNumber) { super(lineNumber) }

    public String toString() { value }
}
