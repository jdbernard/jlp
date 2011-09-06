package com.jdblabs.jlp.ast

public class DocText extends ASTNode {

    public String value

    public DocText(int lineNumber) {
        super(lineNumber)
        value = "" }

    public String toString() { value }
}
