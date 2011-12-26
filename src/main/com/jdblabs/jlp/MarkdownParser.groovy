package com.jdblabs.jlp

import com.jdblabs.jlp.ast.*

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
