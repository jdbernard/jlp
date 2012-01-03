/**
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright JDB Labs 2010-2011
 */
package com.jdblabs.jlp

import com.jdblabs.jlp.ast.*

/**
 * `MarkdownParser` handles `markdown` source files. This parser allows JLP to
 * process plain Markdown files along with the other source files. This is
 * useful as it allows the author to include pages that are pure documentation
 * living alongside the documented source code. Examples would be an overview
 * page, or a quick start guide to the codebase. This parser creates the bare
 * minimum AST structure to include one [`DocBloc`] with the full contents of
 * the source Markdown file.
 *
 * [`DocBlock`]: jlp://jlp.jdb-labs.com/ast/DocBlock
 * @org jlp.jdb-labs.com/MarkdownParser
 */
public class MarkdownParser implements JLPParser {

    public SourceFile parse(String input) {

        /*** Our AST structure will look like this:
           *
           *     SourceFile
           *       Block
           *         DocBlock
           *           DocText
           *             <file-contents>
           *         CodeBlock
           */
        def sourceFile = new SourceFile()
        def block
        def docBlock = new DocBlock(0)
        def codeBlock = new CodeBlock(0)
        def docText = new DocText(0)

        docText.value = input
        docBlock.docTexts << docText
        block = new Block(codeBlock, docBlock, 0)
        sourceFile.blocks << block

        return sourceFile }}
