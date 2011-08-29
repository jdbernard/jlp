package com.jdblabs.jlp

import com.jdblabs.jlp.ast.*

public class EchoEmitter extends JLPEmitter {

    public String emitAuthor(String value) { "Author: $value" }
    public String emitDoc(String value) { value }
    public String emitExample(String value) { "Example:\n$value" }
    public String emitOrg(String value) { "Org: $value" }

    public String emitBlock(TextBlock textBlock) { textBlock.value }
}
