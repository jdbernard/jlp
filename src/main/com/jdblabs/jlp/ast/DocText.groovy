/**
 * # DocText
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright 2011-2012 [JDB Labs LLC](http://jdb-labs.com)
 */
package com.jdblabs.jlp.ast

/**
 * @api ASTNode for `DocTexts`.
 * @org jlp.jdb-labs.com/ast/DocText
 */
public class DocText extends ASTNode {

    public String value

    public DocText(int lineNumber) {
        super(lineNumber)
        value = "" }

    public String toString() { value }
}
