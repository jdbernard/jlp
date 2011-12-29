/**
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright JDB Labs 2010-2011
 */
package com.jdblabs.jlp

import com.jdblabs.jlp.ast.*

/**
 * @org jlp.jdb-labs.com/MarkdownParser
 */
public class MarkdownParser implements JLPParser {

    public SourceFile parse(String input) {

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
