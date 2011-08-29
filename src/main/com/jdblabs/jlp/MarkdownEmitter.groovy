package com.jdblabs.jlp

import com.jdblabs.jlp.ast.*
import org.pegdown.PegDownParser

public class MarkdownEmitter extends JLPEmitter {

    protected MarkdownEmitter() {}

    def pegdown = new PegDownParser()

    protected String emitAuthor(String value) {
        '<span class="author">${value}</span>' }

    protected String emitDoc(String value) { /* parse as MD */ }

    protected String emitExample(String value) {/* parse as MD */ }
    
    protected String emitOrg(String value) {  }

    protected String emitBlock(TextBlock textBlock) { "todo" }

}
