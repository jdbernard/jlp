/**
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright JDB Labs 2010-2011
 */
package com.jdblabs.jlp

import com.jdblabs.jlp.ast.*
import com.jdblabs.jlp.ast.Directive.DirectiveType

import org.pegdown.Extensions
import org.pegdown.PegDownProcessor

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4 as escape

import java.util.List

/**
 * The LiterateMarkdownGenerator is an implementation of JLPBaseGenerator that
 * uses [Markdown](http://daringfireball.net/projects/markdown/) to process the
 * documentation into HTML output.
 * @org jlp.jdb-labs.com/LiterateMarkdownGenerator
 */
public class LiterateMarkdownGenerator extends JLPBaseGenerator {

    /// We will use the PegDown library for generating the Markdown output.
    protected PegDownProcessor pegdown

    public LiterateMarkdownGenerator(Processor processor) {
        super(processor)

        pegdown = new PegDownProcessor(
            Extensions.TABLES | Extensions.DEFINITIONS) }

    //  ===================================
    /** ### Parse phase implementation. ###  */
    //  ===================================

    /** Implement the parse phase for *Directive* nodes. We are interested
      * specifically in saving the link anchor information from *org*
      * directives. */
    protected void parse(Directive directive) {
        switch(directive.type) {
            case DirectiveType.Org:
                LinkAnchor anchor = new LinkAnchor(
                    id: directive.value,
                    source: directive,
                    sourceDocId: processor.currentDocId)

                processor.linkAnchors[anchor.id] = anchor
                break;
            default:
                break // do nothing
            } }

    /** We are not doing any parsing for *CodeBlocks* or *DocTexts*. We have to
      * implement them, as they are abstract on JLPBaseGenerator. */
    protected void parse(CodeBlock codeBlock) {} // nothing to do
    protected void parse(DocText docText) {} // nothing to do

    //  ==================================
    /** ### Emit phase implementation. ###  */
    //  ==================================

    /** @api Emit a *SourceFile*.
      * Each *SourceFile* becomes one ouput HTML file. This method is the entry
      * point for a file to be emitted. */
    protected String emit(SourceFile sourceFile) {
        StringBuilder sb = new StringBuilder()


        /// Create the HTML head and begin the body. 
        sb.append(
"""<!DOCTYPE html>
<html>
    <head>
        <title>${escape(processor.currentDocId)}</title>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8">
        <link type="text/css" rel="stylesheet" media="all"
            href="${resolveLink('/css/jlp.css')}"></link>

        <!-- syntax highlighting plugin -->
        <link type="text/css" rel="stylesheet" media="all"
            href="${resolveLink('/sh/styles/shCoreDefault.css')}"></link>
        <script type="text/javascript"
            src="${resolveLink('/sh/scripts/XRegExp.js')}"></script>
        <script type="text/javascript"
            src="${resolveLink('/sh/scripts/shCore.js')}"></script>""")

        /// If there is a language-specific brush, include it
        def shBrush = processor.shBrushForSourceType(
            processor.currentDoc.sourceType)

        if (shBrush) { sb.append("""

        <script type="text/javascript"
            src="${resolveLink('/sh/scripts/' + shBrush + '.js')}"></script>""") }

        /// Finish our header and begin the body.
        sb.append("""
        <script type="text/javascript">
            SyntaxHighlighter.defaults.light = true;
            SyntaxHighlighter.defaults.unindent = false;
            SyntaxHighlighter.all();
        </script>
    </head>
    <body>
        <div id="container">
            <table cellpadding="0" cellspacing="0">
                <thead><tr>
                    <th class="docs"><h1>${escape(processor.currentDocId)}</h1></th>
                    <th class="code"/>
                </tr></thead>
                <tbody>""")

        /// Emit all of the blocks in the body of the html file.
        sourceFile.blocks.each { block -> sb.append(emit(block)) }

        /// Create the HTML footer.
        sb.append(
"""                </tbody>
            </table>
        </div>
    </body>
</html>""")

        return sb.toString() }

    /** @api Emit a *Block*. */
    protected String emit(Block block) {
        StringBuilder sb = new StringBuilder()

        /// Look for an `@org` directive in the `Block` (already found in the
        /// parse phase)..
        Directive orgDir = block.docBlock.directives.find {
            it.type == DirectiveType.Org }

        /// Create the `tr` that will hold the `Block`. If we found an `@org`
        /// directive we will add the id here.
        // TODO: should this be escaped?
        if (orgDir) { sb.append("\n<tr id='${orgDir.value}'>") }
        else        { sb.append("<tr>") }

        /// Create the `td` for the documentation.
        sb.append('\n<td class="docs">')
        sb.append(emit(block.docBlock))
        sb.append('</td>')

        /// Create the `td` for the `CodeBlock`
        sb.append('\n<td class="code">')
        sb.append(emit(block.codeBlock))
        sb.append('</td>')

        /// Close the table row.
        sb.append('</tr>') }

    /** @api Emit a *DocBlock*. */
    protected String emit(DocBlock docBlock) {
        /// Create a queue for the doc block elements, we are going to 
        /// sort them by type and line number
        List emitQueue 

        /// Later we will need a string builder to hold our result.
        StringBuilder sb

        /** Add all the directives. We are also assigning priorities here that
          * we will use along with the line numbers to sort the elements. Our
          * goal is to preserve the order of doc blocks and doc-block-like
          * elements (examples, api documentation, etc.) while pushing orgs,
          * authorship and other directives to the top. */
        emitQueue = docBlock.directives.collect { directive ->
            def queueItem = [lineNumber: directive.lineNumber, value: directive]
            switch(directive.type) {
                case DirectiveType.Api:         queueItem.priority = 50; break
                case DirectiveType.Author:      queueItem.priority = 10; break
                case DirectiveType.Copyright:   queueItem.priority = 11; break
                case DirectiveType.Example:     queueItem.priority = 50; break
                case DirectiveType.Include:     queueItem.priority = 50; break
                case DirectiveType.Org:         queueItem.priority =  0; break }
            
            return queueItem }

        /// Add all the doc text blocks.
        emitQueue.addAll(docBlock.docTexts.collect { docText ->
            [lineNumber: docText.lineNumber, priority: 50, value: docText] })
                        

        /// Sort the emit queue by priority, then line number.
        emitQueue.sort(
            {i1, i2 -> i1.priority != i2.priority ?
                        i1.priority - i2.priority :
                        i1.lineNumber - i2.lineNumber} as Comparator)
    
        /** Finally, we want to treat the whole block as one markdown chunk so
          * we will concatenate the values in the emit queue and then send the
          * whole block at once to the markdown processor. */
        sb = new StringBuilder()
        emitQueue.each { queueItem -> sb.append(emit(queueItem.value)) } 

        return processMarkdown(sb.toString())
    }

    /** @api Emit a *CodeBlock*. */
    protected String emit(CodeBlock codeBlock) {
        def codeLines

        /// Collect the lines into an array.
        codeLines = codeBlock.lines.collect { lineNumber, line ->
            [lineNumber, line] }

        /// Sort by line number.
        codeLines.sort({ i1, i2 -> i1[0] - i2[0] } as Comparator)

        codeLines = codeLines.collect { arr -> arr[1] }

        /// Write out the lines in a `<pre>` block
        return "<pre class=\"brush: ${processor.currentDoc.sourceType};\">" +
            "${escape(codeLines.join(''))}</pre>" }

    /** @api Emit a *DocText*. */
    protected String emit(DocText docText) { return docText.value }

    /** @api Emit a *Directive*. */
    protected String emit(Directive directive) {
        switch(directive.type) {

            /** An `@api` directive is immediately processed and wrapped in a
              * div (we need to process this now because Markdown does not
              * process input inside HTML elements). */
            case DirectiveType.Api:
                return "<div class='api'>" +
                    processMarkdown(directive.value) + "</div>\n"

            /// `@author` directive is turned into a definition list.

            case DirectiveType.Author:
                return "Author\n:   ${directive.value}\n"

            case DirectiveType.Copyright:
                return "\n&copy; ${directive.value}\n"

            /// An `@example` directive is returned as is.
            case DirectiveType.Example:
                return directive.value

            // TODO:
            // case DirectiveType.Include:

            /// An `@org` directive is ignored here. We already emitted the id
            /// when we started the block.
            case DirectiveType.Org: return "" } }

    /** This is a helper method to process a block of text as Markdown. We need
      * to do some additional processing to deal with `jlp://` org links that
      * may be present. */
    protected String processMarkdown(String markdown) {

        /// Convert to HTML from Markdown
        String html = pegdown.markdownToHtml(markdown)

        /// Replace internal `jlp://` links with actual links based on`@org`
        /// references.
        html = html.replaceAll(/href=['"](jlp:\/\/[^\s"']+)['"]/) { match, link->
            return 'href="' + resolveLink(link) + '"' }

        return html; }

    /// Shortcut for `processor.resolveLink(url, processor.currentDoc)`.
    protected String resolveLink(String url) {
        processor.resolveLink(url, processor.currentDoc) }

}
