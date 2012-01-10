/**
 * # JLPBaseGenerator
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright 2011-2012 [JDB Labs LLC](http://jdb-labs.com)
 */
package com.jdblabs.jlp

import com.jdblabs.jlp.ast.*
import java.util.List
import java.util.Map

/**
 * This class defines the interface for JLP Generators and implements basic
 * functionality for the parse phase. 
 * @org jlp.jdb-labs.com/JLPBaseGenerator
 */
public abstract class JLPBaseGenerator {

    /**
     * The generator works in close conjunction with a JLP Processor.
     * This tight coupling in intended for these two classes. The distiction
     * between the two classes is scope. The Processor class is concerned with
     * data and behavior common to the whole documentation process whereas the
     * Generator is concerned only with one file. The Generator does have
     * access to information about the overall process through this link to the
     * Processor. One example of the need for this link is the resolution of
     * JLP link targets which may be defined in a different file but referenced
     * in the file the Generator is processing.
     * @org jlp.jdb-labs.com/notes/processor-generator-coupling
     */
    protected Processor processor

    protected JLPBaseGenerator(Processor processor) {
        this.processor = processor }

    /**
     * ### Generator phases
     * 
     * There are two phases for JLP Generators: **parse** and **emit**. 
     *
     * The **parse** phase allows the Generator to build up facts about the
     * file being processed before emitting the documentation in the **emit**
     * phase. There is a `parse` method for each [`ASTNode`] type. The default
     * implementation for JLPBaseGenerator calls the `parse` methods in such a
     * way that it visits each `ASTNode` in the file.
     *
     * The **emit** phase is where the Generator creates the documentation for
     * each `ASTNode`. Unlike the parse phase, there is no default implementation
     * for the emit phase as emitting the final result will be very dependant on
     * the emitter.
     *
     * [`ASTNode`]: jlp://jlp.jdb-labs.com/ast/ASTNode
     *
     * @org com.jdb-labs.jlp.JLPBaseGenerator/phases
     */
    protected void parse(SourceFile sourceFile) {
        sourceFile.blocks.each { block -> parse(block) } }

    protected void parse(Block block) {
        parse(block.docBlock)
        parse(block.codeBlock) }

    protected void parse(DocBlock docBlock) {
        docBlock.directives.each { directive -> parse(directive) }
        docBlock.docTexts.each { docText -> parse(docText) } }

    protected abstract void parse(Directive directive)
    protected abstract void parse(CodeBlock codeBlock)
    protected abstract void parse(DocText docText)

    protected abstract String emit(SourceFile sourceFile)
    protected abstract String emit(Block block)
    protected abstract String emit(DocBlock docBlock)
    protected abstract String emit(CodeBlock codeBlock)
    protected abstract String emit(DocText docText)
    protected abstract String emit(Directive directive)

}
