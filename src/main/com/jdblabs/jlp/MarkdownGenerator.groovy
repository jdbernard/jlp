package com.jdblabs.jlp

import com.jdblabs.jlp.ast.*
import com.jdblabs.jlp.ast.Directive.DirectiveType
import com.jdblabs.jlp.ast.TextBlock.TextBlockType

import org.pegdown.PegDownProcessor
import org.pegdown.Extensions

public class MarkdownGenerator extends JLPBaseGenerator {

    protected PegDownProcessor pegdown

    protected MarkdownGenerator() {
        super()

        pegdown = new PegDownProcessor(Extensions.TABLES) }

    protected static Map<String, String> generateDocuments(
    Map<String, List<ASTNode>> sources) {
        MarkdownGenerator inst = new MarkdownGenerator()
        return inst.generate(sources) }

    protected String emit(TextBlock textBlock) {
        switch (textBlock.type) {

            // text block, just convert to markdown
            case TextBlockType.TextBlock:
                return formatText(textBlock.value)

            // code block, we want to emit as a code snippet
            case TextBlockType.CodeBlock:
                // so prepend all lines with four spaces to tell markdown that
                // this is code
                String value = textBlock.value.replaceAll(/(^|\n)/, /$1    /)
                // then convert to markdown
                return pegdown.markdownToHtml(value) } }

    protected String emit(Directive d) {
        switch (d.type) {
            case DirectiveType.Author:
                return "<span class='author'>Author: ${formatText(d.value)}</span>"
            case DirectiveType.Doc:
                return formatText(d.value)
            case DirectiveType.Example:
                return formatText(d.value)
            case DirectiveType.Org:
                docState.orgs[d.value] = [line: d.lineNumber,
                                          file: docState.currentDocId]
                return "<a name='${d.value}'/>" }
    }

    protected String formatText(String text) {

        // convert to HTML from Markdown
        String md = pegdown.markdownToHtml(text)

        // replace internal `jlp://` links with actual links based on`@org`
        // references
        md = md.replaceAll(/jlp:\/\/([^\s"])/, /#$1/)

        return md;
    }
}
