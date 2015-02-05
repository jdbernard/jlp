/**
 * # TargetDoc
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright 2011-2012 [JDB Labs LLC](http://jdb-labs.com)
 */
package com.jdblabs.jlp

import com.jdblabs.jlp.ast.SourceFile

/**
 * @api TargetDoc is a data class to hold information about the output file.
 * @org jlp.jdb-labs.com/TargetDoc
 */
public class TargetDoc {

    /// The result of parsing the input file. 
    public SourceFile sourceAST

    /// The original source file.
    public File sourceFile
    public String sourceDocId

    /// The source code type (ie. `java`, `erlang`, etc.). See
    /// [`Processor.sourceTypeForFile`](jlp.jdb-labs.com/Processor/sourceTypeForFile)
    public String sourceType

    public String output
}
