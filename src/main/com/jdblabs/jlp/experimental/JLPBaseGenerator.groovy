package com.jdblabs.jlp.experimental

import com.jdblabs.jlp.experimental.ast.*
import java.util.List
import java.util.Map

public abstract class JLPBaseGenerator {

    protected Map docState

    protected JLPBaseGenerator() {
        docState = [orgs:           [:],        // stores `@org` references
                    codeTrees:      [:],        // stores code ASTs for
                    currentDocId:   false ] }   // store the current docid

    protected Map<String, String> generate(Map<String, SourceFile> sources) {
        Map result = [:]

        // run the parse phase
        sources.each { sourceId, sourceAST ->

            // set up the current generator state for this source
            docState.currentDocId = sourceId
            docState.codeTrees[sourceId] = sourceAST.codeAST

            parse(sourceAST) }

        // run the emit phase
        sources.each { sourceId, sourceAST ->

            // set up the current generator state for this source
            docState.currentDocId = sourceId

            // generate the doc for this source
            result[sourceId] = emit(sourceAST) }

        // return our results
        return result }

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
