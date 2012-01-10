/**
 * # Block
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright 2011-2012 [JDB Labs LLC](http://jdb-labs.com)
 */
package com.jdblabs.jlp.ast

/**
 * ASTNode for `Blocks`.
 * @org jlp.jdb-labs.com/ast/Block
 */
public class Block extends ASTNode {
    public final DocBlock docBlock
    public final CodeBlock codeBlock

    public Block(CodeBlock cb, DocBlock db, int lineNum) {
        super(lineNum); docBlock = db; codeBlock = cb }

    public String toString() {
        "[${lineNumber}:Block: ${docBlock}, ${codeBlock}]" }
}
