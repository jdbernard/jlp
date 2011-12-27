/**
 * @author Jonathan Bernard
 * @copyright JDB Labs 2010-2011
 */
package com.jdblabs.jlp.ast

/**
 * The top-level AST element. This represents a source file.
 * @org jlp.jdb-labs.com/ast/SourceFile
 */
public class SourceFile {
    public List<ASTNode> blocks = []
    public def codeAST

    public String id
}
