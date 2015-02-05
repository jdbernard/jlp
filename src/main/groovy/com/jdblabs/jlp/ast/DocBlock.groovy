/**
 * # DocBlock
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright 2011-2012 [JDB Labs LLC](http://jdb-labs.com)
 */
package com.jdblabs.jlp.ast

import java.util.ArrayList
import java.util.List

/**
 * @api ASTNode for `DocBlocks`.
 * @org jlp.jdb-labs.com/ast/DocBlock
 */
public class DocBlock extends ASTNode {
    
    public List<Directive> directives = new ArrayList<Directive>()
    public List<DocText> docTexts = new ArrayList<DocText>()

    public DocBlock(int lineNumber) { super(lineNumber) }

    public String toString() {
        "[${lineNumber}:DocBlock: Directives ${directives}, DocTexts ${docTexts}]" }
}
