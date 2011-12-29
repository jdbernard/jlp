/**
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright JDB Labs 2010-2011
 */
package com.jdblabs.jlp.ast

/**
 * ASTNode for *Block*s.
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
