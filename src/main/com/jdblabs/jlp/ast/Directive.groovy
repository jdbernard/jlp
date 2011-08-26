package com.jdblabs.jlp.ast

public class Directive implements ASTNode {

    public static enum DirectiveType {
        Author,
        Doc,
        Example,
        Org;
        
        public static DirectiveType parse(String typeString) {
            valueOf(typeString.toLowerCase().capitalize()) } }

    public final DirectiveType type;
    public final String value;
    public final int lineNumber;

    public Directive(String value, String typeString, int lineNumber) {
        this.value = value
        this.type = DirectiveType.parse(typeString)
        this.lineNumber = lineNumber }
        
    public int getLineNumber() { return lineNumber }

    public String toString() { return "[Directive(${lineNumber}): ${type}, ${value}]" }
}
