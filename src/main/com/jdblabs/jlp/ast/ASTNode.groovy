/**
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright JDB Labs 2010-2011
 */
package com.jdblabs.jlp.ast

/**
 * This defines the interface for nodes of the source file parse tree.
 * @org jlp.jdb-labs.com/ast/ASTNode
 */
public class ASTNode {

    /// This is the line number the element began on.
    protected int lineNumber

    public ASTNode(int lineNum) { this.lineNumber = lineNum }

    public int getLineNumber() { lineNumber }
}
