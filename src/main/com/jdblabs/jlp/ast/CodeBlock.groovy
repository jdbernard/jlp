/**
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright JDB Labs 2010-2011
 * @org jlp.jdb-labs.com/ast/CodeBlock
 */
package com.jdblabs.jlp.ast

import java.util.Map

/**
 * @api ASTNode for `CodeBlocks`.
 * @org jlp.jdb-labs.com/ast/CodeBlock
 */
public class CodeBlock extends ASTNode {

    public Map<Integer, String> lines = [:]

    public CodeBlock(int lineNumber) { super(lineNumber) }

    public String toString() {
        def linesVal = ""
        lines.each { lineNum, value -> linesVal += "${lineNum}:${value}" }
            
        return "[${lineNumber}:CodeBlock: ${linesVal}]" }
}
