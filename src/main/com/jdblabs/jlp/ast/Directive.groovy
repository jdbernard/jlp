package com.jdblabs.jlp.ast

public class Directive extends ASTNode {

    public static enum DirectiveType {
        Api, Author, Copyright, Example, Org;
        
        public static DirectiveType parse(String typeString) {
            valueOf(typeString.toLowerCase().capitalize()) } }

    public final DirectiveType type;
    public final String value;

    public Directive(String value, String typeString, int lineNumber) {
        super(lineNumber)
        this.value = value
        this.type = DirectiveType.parse(typeString) }
        
    public String toString() { "[${lineNumber}:Directive: ${type}, ${value}]" }
}
