package com.jdblabs.jlp.ast

public class TextBlock implements ASTNode {

    public static enum TextBlockType { TextBlock, CodeBlock }

    public final TextBlockType type
    public final String value
    public final int lineNumber

    public TextBlock(TextBlockType type, String value, int lineNumber) {
        this.type = type
        this.value = value
        this.lineNumber = lineNumber }

    public int getLineNumber() { return lineNumber }

    public String toString() { return "[${type}(${lineNumber}): ${value}]" }

    public static TextBlock makeTextBlock(String value, int lineNumber) {
        return new TextBlock(TextBlockType.TextBlock, value, lineNumber) }

    public static TextBlock makeCodeBlock(String value, int lineNumber) {
        return new TextBlock(TextBlockType.CodeBlock, value, lineNumber) }
}
