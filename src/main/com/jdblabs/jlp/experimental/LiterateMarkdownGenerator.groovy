package com.jdblabs.jlp.experimental

import com.jdblabs.jlp.experimental.ast.*
import com.jdblabs.jlp.experimental.ast.Directive.DirectiveType

import org.pegdown.Extensions
import org.pegdown.PegDownProcessor

import java.util.List

public class LiterateMarkdownGenerator extends JLPBaseGenerator {

    protected PegDownProcessor pegdown

    protected LiterateMarkdownGenerator() {
        super()

        pegdown = new PegDownProcessor(
            Extensions.TABLES & Extensions.DEFINITIONS) }

    protected static Map<String, String> generateDocuments(
    Map<String, SourceFile> sources) {
        LiterateMarkdownGenerator inst = new LiterateMarkdownGenerator()
        return inst.generate(sources) }

    protected String emit(SourceFile sourceFile) {
        StringBuilder sb = new StringBuilder()


        // Create the HTML file head
        sb.append(
"""<!DOCTYPE html>
<html>
    <head>
        <title>${docState.currentDocId}</title>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8">
        <!-- <link rel="syltesheet" media="all" href=""/> -->
    </head>
    <body>
        <div id="container">
            <table cellpadding="0" cellspacing="0">
                <thead><tr>
                    <th class="doc"><h1>${docState.currentDocId}</h1></th>
                    <th class="code"/>
                </tr></thead>
                <tbody>""")

        // Emit the document to Markdown
        sourceFile.blocks.each { block -> sb.append(emit(block)) }

        // Create the HTML file foot
        sb.append(
"""                </tbody>
            </table>
        </div>
    </body>
</html>""")

        return sb.toString() }

    protected String emit(Block block) {
        StringBuilder sb = new StringBuilder()

        // Look for an `@org` directive in the `Block`
        Directive orgDir = block.docBlock.directives.find {
            it.type == DirectiveType.Org }

        // Create the `tr` that will hold the `Block`
        if (orgDir) { sb.append("\n<tr id='${orgDir.value}'>") }
        else        { sb.append("<tr>") }

        // Create the `td` for the documentation.
        sb.append('\n<td class="doc">')
        sb.append(emit(block.docBlock))
        sb.append('</td>')

        // Create the `td` for the `CodeBlock`
        sb.append('\n<td class="code">')
        sb.append(emit(block.codeBlock))
        sb.append('</td>')

        // Close the table row.
        sb.append('</tr>') }

    protected String emit(DocBlock docBlock) {
        // Create a queue for the doc block elements, we are going to 
        // sort them by type and line number
        List emitQueue 

        // Later we will need a string builder to hold our result.
        StringBuilder sb

        // Add all the directives
        emitQueue = docBlock.directives.collect { directive ->
            def queueItem = [lineNumber: directive.lineNumber, value: directive]
            switch(directive.type) {
                case DirectiveType.Api:         queueItem.priority = 12; break
                case DirectiveType.Author:      queueItem.priority = 10; break
                case DirectiveType.Copyright:   queueItem.priority = 11; break
                case DirectiveType.Example:     queueItem.priority = 50; break
                case DirectiveType.Org:
                    docState.orgs[directive.value] = directive
                    queueItem.priority = 0
                    break }
            
            return queueItem }

        // Add all the doc text blocks
        emitQueue.addAll(docBlock.docTexts.collect { docText ->
            [lineNumber: docText.lineNumber, priority: 50, value: docText] })
                        

        println emitQueue
        println "----------"

        // Sort the emit queue by priority, then line number.
        emitQueue.sort(
            {i1, i2 -> i1.priority != i2.priority ?
                        i1.priority - i2.priority :
                        i1.line - i2.line} as Comparator)
    
        // Finally, we want to treat the whole block as one markdown chunk, so
        // we will concatenate the values in the emit queue and then process
        // the whole block once
        sb = new StringBuilder()
        emitQueue.each { queueItem -> sb.append(emit(queueItem.value)) } 

        return pegdown.markdownToHtml(sb.toString())
    }

    protected String emit(CodeBlock codeBlock) {
        def codeLines

        // Collect the lines into an array.
        codeLines = codeBlock.lines.collect { lineNumber, line ->
            [lineNumber, line] }

        // Sort by line number.
        codeLines.sort({ i1, i2 -> i1[0] - i2[0] } as Comparator)

        codeLines = codeLines.collect { arr -> arr[1] }

        // write out the lines in a <pre><code> block
        return "<pre><code>${codeLines.join('')}</code></pre>" }

    protected String emit(DocText docText) { return docText.value }

    protected String emit(Directive directive) {
        switch(directive.type) {

            // An `@api` directive is immediately processed and wrapped in a
            // div (we need to process this now because Markdown does not
            // process input inside HTML elements).
            case DirectiveType.Api:
                return "<div class='api'>" +
                    pegdown.markdownToHtml(directive.value) + "</div>\n"

            // An `@author` directive is turned into a definition list.
            case DirectiveType.Author:
                return "Author\n:   ${directive.value}\n"

            case DirectiveType.Copyright:
                return "&copy; ${directive.value}\n"

            // An `@example` directive is returned as is
            case DirectiveType.Example: return directive.value

            // An `@org` directive is ignored.
            case DirectiveType.Org: return "" }
    }
}
