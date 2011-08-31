package com.jdblabs.jlp;

import com.jdblabs.jlp.ast.*;
import java.util.ArrayList;
import java.util.List;
import org.parboiled.Action;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.annotations.*;

import static com.jdblabs.jlp.ast.TextBlock.makeCodeBlock;
import static com.jdblabs.jlp.ast.TextBlock.makeTextBlock;

@BuildParseTree
public class JLPPegParser extends BaseParser<Object> {

    int curLineNum = 1;

    public Rule SourceFile() {
        return Sequence(
            clearLineCount(),
            push(new ArrayList<ASTNode>()),
            ZeroOrMore(Sequence(
                FirstOf(
                    DocBlock(),
                    CodeBlock()),
                push(addToList(pop(), (List<Object>)pop()))))); }

    /**
     * Parses the rule:
     *  DocBlock = (DirectiveBlock / DocTextBlock)+
     *
     * Pushes a DocBlock object onto the stack.
     */
    Rule DocBlock() {
        return Sequence(
            push(new DocBlock(curLineNum)),
            OneOrMore(
                FirstOf(
                    Sequence(DirectiveBlock(),
                        push(addDirectiveBlock((Directive) pop(), (DocBlock) pop()))),
                    Sequence(DocTextBlock(),
                        push(addTextBlock((TextBlock) pop(), (DocBlock) pop())))))); }

    /**
     * Parses the rule:
     *  CodeBlock = !DocStart RemainingLine
     *
     * Pushes a CodeBlock onto the stack.
     */
    Rule CodeBlock() {
        return Sequence(
            push(curLineNum),
            push(""),
            OneOrMore(Sequence(
                TestNot(DocStart()), RemainingLine(),
                push(popAsString() + match()))),

            push(makeCodeBlock(popAsString(),popAsInt()))); }
                
    /**
     * Parses the rule:
     *  DocStart = SPACE* DOC_START
     */
     Rule DocStart() {
        return Sequence(ZeroOrMore(SPACE), DOC_START); }
            
    /**
     * Parses the rule:
     *  DirectiveBlock =
     *      DocStart DIRECTIVE_START (LongDirective / LineDirective)
     *
     * Pushes a Directive onto the stack.
     */
    Rule DirectiveBlock() {
        return Sequence(
            DocStart(), DIRECTIVE_START,
            FirstOf(LongDirective(), LineDirective())); }

    /**
     * Parses the rule:
     *  LongDirective =
     *      (AUTHOR_DIR / DOC_DIR / EXAMPLE_DIR) RemainingLine DocTextBlock?
     * 
     * Pushes a Directive object onto the value stack.
     */
    Rule LongDirective() {
        return Sequence(
            push(curLineNum),
            FirstOf(AUTHOR_DIR, DOC_DIR, EXAMPLE_DIR),  push(match()),
            RemainingLine(),                            push(match()),
            Optional(Sequence(
                DocTextBlock(),                        // pushes block
                swap(),
                push(popAsString() + ((TextBlock) pop()).value))),
            
            // pull off the value, type and create the directive
            push(new Directive(popAsString(), popAsString(), popAsInt()))); }

    /**
     * Parses the rule:
     *  LineDirective = 
     *      ORG_DIR RemainingLine
     *
     * Pushes a Directive object onto the value stack.
     */
    Rule LineDirective() {
        return Sequence(
            push(curLineNum),
            ORG_DIR,            push(match()),
            RemainingLine(),

            // pull off the value, type and create the directive
            push(new Directive(match().trim(), popAsString(), popAsInt()))); }

    /**
     * Parses the rule:
     *  DocTextBlock =  DocTextLine+
     *
     * Pushes a DocTextBlock onto the stack as a string.
     */
    Rule DocTextBlock() {
        return Sequence(
            push(curLineNum),
            DocTextLine(), // pushes the value onto the stack
            ZeroOrMore(Sequence(
                DocTextLine(),
                swap(),
                push(popAsString() + popAsString()))),
            
            push(makeTextBlock(popAsString(), popAsInt()))); }

    /**
     * Parses the rule:
     *  DocTextLine =
     *      DocStart !DIRECTIVE_START RemainingLine
     *
     * Pushes the line value (not including the DocStart) onto the stack.
     */
    Rule DocTextLine() {
        return Sequence(
            DocStart(), TestNot(DIRECTIVE_START),
            RemainingLine(), push(match())); }

    /**
     * Parses the rule:
     *  RemainingLine = (!EOL)+ EOL
     */
    @SuppressSubnodes
    Rule RemainingLine() {
        return FirstOf(
            Sequence(ZeroOrMore(NOT_EOL), EOL, incLineCount()),

            // allow EOI as a line delimiter only if the line is not empty,
            // otherwise it will match infinitely if RemainingLine is used in a
            // OneOrMore context.
            Sequence(OneOrMore(NOT_EOL), EOI)); }    

    Rule DOC_START      = String("%% ");
    Rule EOL            = Ch('\n');
    Rule NOT_EOL        = Sequence(TestNot(EOL), ANY);
    Rule DIRECTIVE_START= Ch('@');
    Rule SLASH          = Ch('/');
    Rule SPACE          = AnyOf(" \t");

    // directive terminals
    Rule AUTHOR_DIR   = IgnoreCase("author");
    Rule DOC_DIR      = IgnoreCase("doc");
    Rule EXAMPLE_DIR  = IgnoreCase("example");
    Rule ORG_DIR      = IgnoreCase("org");

    String popAsString() { return (String) pop(); }

    Integer popAsInt() { return (Integer) pop(); }

    static <T> List<T> addToList(T value, List<T> list) {
        list.add(value);
        return list; }

    boolean printValueStack() {
        for (int i = 0; i < getContext().getValueStack().size(); i++) {
            System.out.println(i + ": " + peek(i)); }
        return true; }

    boolean clearLineCount() { curLineNum = 1; return true; }

    boolean incLineCount() { curLineNum++; return true; }

    boolean echo(String msg) { System.out.println(msg); return true; }

    static DocBlock addDirectiveBlock(Directive dir, DocBlock docBlock) {
        docBlock.directives.add(dir); return docBlock; }

    static DocBlock addTextBlock(TextBlock tb, DocBlock docBlock) {
        docBlock.textBlocks.add(tb); return docBlock; }
}
