package com.jdblabs.jlp

@BuildParseTree
public class JLPPegParser extends BaseParser<Object> {

    Rule CodePage() {
        return ZeroOrMore(FirstOf(
            DocBlock(),
            CodeBlock())) }

    Rule DocBlock() {
        return OneOrMore(FirstOf(
            DirectiveBlock(),
            MarkdownBlock())) }

    Rule DirectiveBlock() {
        return FirstOf(
            Sequence("%% "
}
