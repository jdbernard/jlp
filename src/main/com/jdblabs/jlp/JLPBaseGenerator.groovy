package com.jdblabs.jlp

import com.jdblabs.jlp.ast.*
import java.util.List
import java.util.Map

public abstract class JLPBaseGenerator {

    protected Processor processor

    protected JLPBaseGenerator(Processor processor) {
        this.processor = processor }

    protected String generate(SourceFile source) {

        // run the parse phase
        parse(source)

        // run the emit phase
        return emit(source) }

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
