package com.jdblabs.jlp.ast

public class Block extends ASTNode {
    public final DocBlock docBlock
    public final CodeBlock codeBlock

    public Block(CodeBlock cb, DocBlock db, int lineNum) {
        super(lineNum); docBlock = db; codeBlock = cb }

    public String toString() {
        "[${lineNumber}:Block: ${docBlock}, ${codeBlock}]" }
}
