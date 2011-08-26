package com.jdblabs.jlp;

import org.parboiled.Action;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.annotations.*;

public class JLPPegParser extends BaseParser<Object> {

    public Rule CodePage() {
        return ZeroOrMore(FirstOf(
            DocBlock(),
            CodeBlock())); }

    Rule DocBlock() {
        return OneOrMore(FirstOf(
            DirectiveBlock(),
            MarkdownBlock())); }

    Rule CodeBlock() {
        return OneOrMore(Sequence(
            TestNot(DOC_START), RemainingLine())); }

    Rule DirectiveBlock() {
        return FirstOf(

            // there is a bug in parboiled that prevents sequences of greater
            // than 2, so this ia workaround
            Sequence(DOC_START, DIRECTIVE_START, LongDirective(),
                RemainingLine(), Optional(MarkdownBlock())),

            Sequence(DOC_START, DIRECTIVE_START, LineDirective(),
                RemainingLine())); }

    Rule LongDirective() { return FirstOf(AUTHOR_DIR, DOC_DIR, EXAMPLE_DIR); }

    Rule LineDirective() { return ORG_DIR; }

    Rule MarkdownBlock() { return OneOrMore(MarkdownLine()); }

    Rule MarkdownLine() {
        return Sequence(DOC_START, TestNot(DIRECTIVE_START), RemainingLine()); }

    Rule RemainingLine() { return Sequence(OneOrMore(NOT_EOL), EOL); }

    Rule DOC_START      = String("%% ");
    Rule EOL            = Ch('\n');
    Rule NOT_EOL        = Sequence(TestNot(EOL), ANY);
    Rule DIRECTIVE_START= Ch('@');
    Rule SLASH          = Ch('/');

    // directive terminals
    Rule AUTHOR_DIR   = IgnoreCase("author");
    Rule DOC_DIR      = IgnoreCase("doc");
    Rule EXAMPLE_DIR  = IgnoreCase("example");
    Rule ORG_DIR      = IgnoreCase("org");
}
