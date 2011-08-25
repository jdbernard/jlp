package com.jdblabs.jlp

import org.parboiled.BaseParser
import org.parboiled.Rule
import org.parboiled.annotations.*

public class JLPPegParser extends BaseParser<Object> {

    public Rule CodePage() {
        println "Parsing CodePage"
        ZeroOrMore(FirstOf(
            DocBlock(),
            CodeBlock())) }

    Rule DocBlock() {
        OneOrMore(FirstOf(
            DirectiveBlock(),
            MarkdownBlock())) }

    Rule CodeBlock() {
        OneOrMore(Sequence(
            TestNot(DOC_START), RemainingLine())) }

    Rule DirectiveBlock() {
        FirstOf(

            // there is a bug in parboiled that prevents sequences of greater
            // than 2, so this ia workaround
            Sequence(
                Sequence(
                    Sequence(DOC_START, DIRECTIVE_START),
                    Sequence(LongDirective(), RemainingLine())),
                Sequence(Optional(MarkdownBlock()))),

            Sequence(
                Sequence(DOC_START, DIRECTIVE_START),
                Sequence(LineDirective(), RemainingLine()))) }

    Rule LongDirective() { FirstOf(AUTHOR_DIR, DOC_DIR, EXAMPLE_DIR) }

    Rule LineDirective() { ORG_DIR }

    Rule MarkdownBlock() { OneOrMore(MarkdownLine()) }

    Rule MarkdownLine() {
        Sequence(DOC_START, Sequence(TestNot(DIRECTIVE_START), RemainingLine())) }

    Rule RemainingLine() { Sequence(OneOrMore(NOT_EOL), EOL) }

    Rule DOC_START      = String("%% ")
    Rule EOL            = Ch('\n' as char)
    Rule NOT_EOL        = Sequence(TestNot(EOL), ANY)
    Rule DIRECTIVE_START= Ch('@' as char)
    Rule SLASH          = Ch('/' as char)

    // directive terminals
    Rule AUTHOR_DIR   = IgnoreCase("author")
    Rule DOC_DIR      = IgnoreCase("doc")
    Rule EXAMPLE_DIR  = IgnoreCase("example")
    Rule ORG_DIR      = IgnoreCase("org")

}
