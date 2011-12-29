/**
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright JDB Labs 2010-2011
 */
package com.jdblabs.jlp.ast

/**
 * @api ASTNode for *DocText*s.
 * @org jlp.jdb-labs.com/ast/DocText
 */
public class DocText extends ASTNode {

    public String value

    public DocText(int lineNumber) {
        super(lineNumber)
        value = "" }

    public String toString() { value }
}
