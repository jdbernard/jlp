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

    /** The source file gets split into two parts during the initial parsing by
      * the PEG grammer: a list of documentation blocks and a list of code
      * blocks. Both are stored in the `Blocks` but the code block may get
      * processed again if there is a language-specific parser involved for code
      * awareness. In this case, we will keep a copy of the result of that
      * parsing here as well. */

    /// A list of the blocks in this `SourceFile`.
    public List<ASTNode> blocks = []

    /// The result from parsing the code in this source file. Currently there
    /// are no language-specific parsers and this is always `null`.
    public def codeAST

    /// The id for this source file, currently set to the path name for the
    /// input file relative to the input root.
    public String id
}
