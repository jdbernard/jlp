/**
 * # LiterateMarkdownGenerator
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright 2011-2012 [JDB Labs LLC](http://jdb-labs.com)
 */
package com.jdblabs.jlp

import com.jdblabs.jlp.ast.*
import com.jdblabs.jlp.LinkAnchor.LinkType
import com.jdblabs.jlp.ast.Directive.DirectiveType

import org.pegdown.Extensions
import org.pegdown.PegDownProcessor

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4 as escape

import java.util.List

/**
 * The LiterateMarkdownGenerator is an implementation of [`JLPBaseGenerator`]
 * that uses [Markdown] to process the documentation into HTML output.
 *
 * [`JLPBaseGenerator`]: jlp://jlp.jdb-labs.com/JLPBaseGenerator
 * [Markdown]: http://daringfireball.net/projects/markdown/
 * @org jlp.jdb-labs.com/LiterateMarkdownGenerator
 */
public class LiterateMarkdownGenerator extends JLPBaseGenerator {

    /// We will use the [PegDown](https://github.com/sirthias/pegdown) library
    /// for generating the Markdown output.
    protected PegDownProcessor pegdown

    public LiterateMarkdownGenerator(Processor processor) {
        super(processor)

        pegdown = new PegDownProcessor(
            Extensions.TABLES | Extensions.DEFINITIONS) }

    //  ===================================
    /** ### Parse phase implementation. ###  */
    //  ===================================

    /** Override the parse phase for [`SourceFile`] nodes. We are interested in
      * detecting an `org` directive in the first DocBlock, or automatically
      * creating one if it is not defined. The first `org` directive (found or
      * created) will create a FileLink type LinkAnchor.
      */

    protected void parse(SourceFile sourceFile) {
        /// First we look for an `org` directive in the first block.
        def firstOrg = sourceFile.blocks[0].docBlock.directives.find {
            it.type == DirectiveType.Org }

        /// And we create one if there are none.
        if (!firstOrg) {
            def docBlock = sourceFile.blocks[0].docBlock
            firstOrg = new Directive(processor.currentDocId, 'org', 0, docBlock)
            docBlock.directives << firstOrg }

        /// Now parse the file as usual.
        super.parse(sourceFile)

        /// And mark the first `org` as a FileLink
        processor.linkAnchors[firstOrg.value].type = LinkType.FileLink}

    /** Override the parse phase for [`DocBlock`] nodes. We are interested in
      * detecting a block that has multilple `org` directives. When there are
      * multiple org directives in one block we change the LinkAnchor type from
      * block-level links to specific anchors in the text. This allows the
      * author to create a link to exact points within the document.
      *
      * [`DocBlock`]: jlp://jlp.jdb-labs.com/ast/DocBlock
      */
    protected void parse(DocBlock docBlock) {
        /// First parse the block as usual.
        super.parse(docBlock)

        /// Look for multiple `org` directives.
        def orgDirectives = docBlock.directives.findAll {it.type == DirectiveType.Org }

        /// If we have multiple `org` directives in one [`DocBlock`] then we
        /// want to change the corresponding [`LinkAnchors`] to type
        /// `AnchorType`.
        ///
        /// [`DocBlock`]: jlp://jlp.jdb-labs.com/ast/DocBlock
        /// [`LinkAnchors`]: jlp://jlp.jdb-labs.com/LinkAnchor
        if (orgDirectives.size() > 1) {
            orgDirectives.each { directive ->
                /// Get the LinkAnchor for this `org` link.
                def linkAnchor = processor.linkAnchors[directive.value]
                linkAnchor.type = LinkType.InlineLink }}}

    /** Implement the parse phase for [`Directive`] nodes. We are interested
      * specifically in saving the link anchor information from *org*
      * directives.
      *
      * [`Directive`]: jlp://jlp.jdb-labs.com/ast/Directive
      */
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

    /** We are not doing any parsing for [`CodeBlocks`] or [`DocTexts`]. We
      * have to implement them, as they are abstract on [`JLPBaseGenerator`].
      *
      * [`CodeBlocks`]: jlp://jlp.jdb-labs.com/ast/CodeBlock
      * [`DocTexts`]: jlp://jlp.jdb-labs.com/ast/DocText
      * [`JLPBaseGenerator`]: jlp://jlp.jdb-labs.com/JLPBaseGenerator
      */
    protected void parse(CodeBlock codeBlock) {} // nothing to do
    protected void parse(DocText docText) {} // nothing to do

    //  ==================================
    /** ### Emit phase implementation. ###  */
    //  ==================================

    /** @api Emit a [`SourceFile`].
      * Each [`SourceFile`] becomes one ouput HTML file. This method is the
      * entry point for a file to be emitted.
      *
      * [`SourceFile`]: jlp://jlp.jdb-labs.com/ast/SourceFile
      */
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
            href="${resolveLink('/.css/jlp.css')}"></link>

        <!-- syntax highlighting plugin -->
        <link type="text/css" rel="stylesheet" media="all"
            href="${resolveLink('/.sh/styles/shCoreDefault.css')}"></link>
        <script type="text/javascript"
            src="${resolveLink('/.sh/scripts/XRegExp.js')}"></script>
        <script type="text/javascript"
            src="${resolveLink('/.sh/scripts/shCore.js')}"></script>""")

        /// If there is a language-specific brush, include it
        def shBrush = processor.shBrushForSourceType(
            processor.currentDoc.sourceType)

        if (shBrush) { sb.append("""

        <script type="text/javascript"
            src="${resolveLink('/.sh/scripts/' + shBrush + '.js')}"></script>""") }

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

    /** @api Emit a [`Block`](jlp://jlp.jdb-labs.com/ast/Block). */
    protected String emit(Block block) {
        StringBuilder sb = new StringBuilder()

        /// Look for an `@org` directive in the `Block` that is marked as a
        /// block link (we may have many `orgs` in a block that are not block
        /// links).
        Directive orgDir = block.docBlock.directives.find { directive ->
            directive.type == DirectiveType.Org &&
            processor.linkAnchors[directive.value]?.type == LinkType.BlockLink }

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

    /** @api Emit a [`DocBlock`](jlp://jlp.jdb-labs.com/ast/DocBlock). */
    protected String emit(DocBlock docBlock) {
        /// Create a queue for the doc block elements, we are going to 
        /// sort them by type and line number
        List emitQueue 

        /// Later we will need a string builder to hold our result.
        StringBuilder sb

        /** We want to treat the whole block as one markdown chunk so we will
          * concatenate the directives and texts and send the whole block at
          * once to the markdown processor.
          */
        emitQueue = docBlock.directives + docBlock.docTexts
        emitQueue.sort { it.lineNumber }
    
        sb = new StringBuilder()
        emitQueue.each { queueItem -> sb.append(emit(queueItem)) } 

        return processMarkdown(sb.toString())
    }

    /** @api Emit a [`CodeBlock`](jlp://jlp.jdb-labs.com/ast/CodeBlock). */
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

    /** @api Emit a [`DocText`](jlp://jlp.jdb-labs.com/ast/DocText). */
    protected String emit(DocText docText) { return docText.value }

    /** @api Emit a [`Directive`](jlp://jlp.jdb-labs.com/ast/Directive). */
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

            case DirectiveType.Param:
                return "" // TODO: can we do better here, even though we're
                           // not understanding the source yet?
            // TODO:
            // case DirectiveType.Include:

            /// An `@org` directive may be emitted if the [`LinkAnchor`] is an
            /// `InlineLink` type.
            ///
            /// [`LinkAnchor`]: jlp://jlp.jdb-labs.com/LinkAnchor
            case DirectiveType.Org: 
                def link = processor.linkAnchors[directive.value]
                if (link.type == LinkType.InlineLink) {
                    return "<a id='${directive.value}'></a>\n" }
                else { return "" }}}

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

    /// Shortcut for [`processor.resolveLink(url, processor.currentDoc)`][RL].
    ///
    /// [RL]: jlp://jlp.jdb-labs.com/Processor/resolveLink
    protected String resolveLink(String url) {
        processor.resolveLink(url, processor.currentDoc) }

}
