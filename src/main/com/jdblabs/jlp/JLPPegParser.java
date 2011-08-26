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
import static com.jdblabs.jlp.ast.TextBlock.makeMarkdownBlock;

@BuildParseTree
public class JLPPegParser extends BaseParser<Object> {

    int curLineNum = 1;

    public Rule SourceFile() {
        return Sequence(
            clearLineCount(),
            push(new ArrayList<Object>()),
            ZeroOrMore(Sequence(
                FirstOf(
                    DocBlock(),
                    CodeBlock()),
                push(addToList(pop(), (List<Object>)pop()))))); }

    /**
     * Parses the rule:
     *  DocBlock = DirectiveBlock / MarkdownBlock
     *
     * Pushes a DocBlock object onto the stack.
     */
    Rule DocBlock() {
        return Sequence(
            push(new ArrayList<ASTNode>()),
            OneOrMore(Sequence(
                FirstOf(
                    DirectiveBlock(),
                    MarkdownBlock()),

                // stack is now: [List<ASTNode>, BlockValue *top*]
                // pop the Block, then List, pass to helper to add the
                // Block to the list, then push the List back on
                push(addToList((ASTNode)pop(), (List<ASTNode>)pop()))))); }

    /**
     * Parses the rule:
     *  CodeBlock = !DOC_START RemainingLine
     *
     * Pushes a CodeBlock onto the stack.
     */
    Rule CodeBlock() {
        return Sequence(
            push(curLineNum),
            TestNot(DOC_START), RemainingLine(), push(match()),
            ZeroOrMore(Sequence(
                TestNot(DOC_START), RemainingLine(),
                push(popAsString() + match()))),
                
            push(makeCodeBlock(popAsString(), popAsInt()))); }

    /**
     * Parses the rule:
     *  DirectiveBlock =
     *      DOC_START DIRECTIVE_START (LongDirective / LineDirective)
     *
     * Pushes a Directive onto the stack.
     */
    Rule DirectiveBlock() {
        return Sequence(
            DOC_START, DIRECTIVE_START,
            FirstOf(LongDirective(), LineDirective())); }

    /**
     * Parses the rule:
     *  LongDirective =
     *      (AUTHOR_DIR / DOC_DIR / EXAMPLE_DIR) RemainingLine MarkdownBlock?
     * 
     * Pushes a Directive object onto the value stack.
     */
    Rule LongDirective() {
        return Sequence(
            push(curLineNum),
            FirstOf(AUTHOR_DIR, DOC_DIR, EXAMPLE_DIR),  push(match()),
            RemainingLine(),                            push(match()),
            Optional(Sequence(
                MarkdownBlock(),                        // pushes block
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
     *  MarkdownBlock =  MarkdownLine+
     *
     * Pushes a MarkdownBlock onto the stack as a string.
     */
    Rule MarkdownBlock() {
        return Sequence(
            push(curLineNum),
            MarkdownLine(), // pushes the value onto the stack
            ZeroOrMore(Sequence(
                MarkdownLine(),
                swap(),
                push(popAsString() + popAsString()))),
            
            push(makeMarkdownBlock(popAsString(), popAsInt()))); }

    /**
     * Parses the rule:
     *  MarkdownLine =
     *      DOC_START !DIRECTIVE_START RemainingLine
     *
     * Pushes the line value (not including the DOC_START) onto the stack.
     */
    Rule MarkdownLine() {
        return Sequence(
            DOC_START, TestNot(DIRECTIVE_START),
            RemainingLine(), push(match())); }

    /**
     * Parses the rule:
     *  RemainingLine = (!EOL)+ EOL
     */
    @SuppressSubnodes
    Rule RemainingLine() {
        return Sequence(OneOrMore(NOT_EOL), EOL, incLineCount()); }

    Rule DOC_START      = String("%% ");
    Rule EOL            = FirstOf(Ch('\n'), EOI);
    Rule NOT_EOL        = Sequence(TestNot(EOL), ANY);
    Rule DIRECTIVE_START= Ch('@');
    Rule SLASH          = Ch('/');

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
}