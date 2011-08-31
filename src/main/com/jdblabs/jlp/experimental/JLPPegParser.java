package com.jdblabs.jlp.experimental;

import com.jdblabs.jlp.experimental.ast.*;
import java.util.ArrayList;
import java.util.List;
import org.parboiled.Action;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.annotations.*;

@BuildParseTree
public class JLPPegParser extends BaseParser<Object> {

    int curLineNum = 1;

    /**
     * Parses the rule:
     *  SourceFile = (Block / DocBlock / CodeBlock)+
     *
     * Pushes a SourceFile node on the stack.
     */
    public Rule SourceFile() {
        return Sequence(
            clearLineCount(),
            push(new SourceFile()),

            OneOrMore(Sequence(
                FirstOf(Block(), DocBlock(), CodeBlock()),

                addBlock((ASTNode) pop())))); }

    /**
     * Parses the rule:
     *   Block = DocBlock CodeBlock
     *
     * Pushes a Block onto the stack
     */
    Rule Block() {
        return Sequence(
            push(curLineNum),
            DocBlock(), CodeBlock(),

            push(new Block((CodeBlock) pop(), (DocBlock) pop(), popAsInt()))); }

    /**
     * Parses the rule:
     *  DocBlock = (Directive / DocText)+
     *
     * Pushes a DocBlock object onto the stack
     */
    Rule DocBlock() {
        return Sequence(
            push(new DocBlock(curLineNum)),
            OneOrMore(Sequence(
                FirstOf(Directive(), DocText()),
                addToDocBlock((ASTNode) pop())))); }

    /**
     * Parses the rule:
     *  CodeBlock = (!DocLineStart RemainingLine)+
     *
     * Pushes a CodeBlock onto the stack.
     */
    Rule CodeBlock() {
        return Sequence(
            push(new CodeBlock(curLineNum)),
            OneOrMore(Sequence(
                TestNot(DocLineStart()), RemainingLine(),
                addToCodeBlock(match())))); }

    /**
     * Parses the rule:
     *  Directive = DocLineStart AT (LongDirective / ShortFirective)
     *
     * Pushes a Directive node on the stack.
     */
    Rule Directive() {
        return Sequence(
            DocLineStart(), AT, FirstOf(LongDirective(), ShortDirective())); }

    /**
     * Parses the rule:
     *  LongDirective =
     *      (AUTHOR_DIR / DOC_DIR / EXAMPLE_DIR) RemainingLine DocText?
     *
     * Pushes a Directive node onto the stack.
     */
    Rule LongDirective() {
        return Sequence(
            push(curLineNum),

            FirstOf(AUTHOR_DIR, DOC_DIR, EXAMPLE_DIR),  push(match()),
            RemainingLine(),                            push(match()),

            Optional(Sequence(
                DocText(),
                swap(),
                push(popAsString() + ((DocText) pop()).value))),
                
            push(new Directive(popAsString(), popAsString(), popAsInt()))); }

    /**
     * Parses the rule:
     *  ShortDirective = (ORG_DIR / COPYRIGHT_DIR) RemainingLine
     *
     * Pushes a Directive node onto the stack.
     */
    Rule ShortDirective() {
        return Sequence(
            push(curLineNum),
            FirstOf(ORG_DIR, COPYRIGHT_DIR), push(match()),
            RemainingLine(),
            
            push(new Directive(match().trim(), popAsString(), popAsInt()))); }

    /**
     * Parses the rule:
     *  DocText = (DocLineStart !AT RemainingLine)+
     *
     * Pushes a DocText node onto the stack.
     */
    Rule DocText() {
        return Sequence(
            push(new DocText(curLineNum)),
            OneOrMore(Sequence(
                DocLineStart(), TestNot(AT), RemainingLine(),
                addToDocText(match())))); }

    @SuppressSubnodes
    Rule DocLineStart() {
        return Sequence(
            ZeroOrMore(SPACE), DOC_LINE_START, Optional(SPACE)); }

    @SuppressSubnodes
    Rule RemainingLine() {
        return FirstOf(
            Sequence(ZeroOrMore(NOT_EOL), EOL, incLineCount()),
            Sequence(OneOrMore(NOT_EOL), EOI, incLineCount())); }

    Rule AT         = Ch('@');
    Rule EOL        = OneOrMore(AnyOf("\r\n"));
    Rule NOT_EOL    = Sequence(TestNot(EOL), ANY);
    Rule SPACE      = AnyOf(" \t");
    Rule DOC_LINE_START = String("%%");

    // directive terminals
    Rule AUTHOR_DIR     = IgnoreCase("author");
    Rule COPYRIGHT_DIR  = IgnoreCase("copyright");
    Rule DOC_DIR        = IgnoreCase("doc");
    Rule EXAMPLE_DIR    = IgnoreCase("example");
    Rule ORG_DIR        = IgnoreCase("org");

    String popAsString() { return (String) pop(); }

    Integer popAsInt() { return (Integer) pop(); }

    boolean clearLineCount() { curLineNum = 1; return true; }

    boolean incLineCount() { curLineNum++; return true; }

    boolean addBlock(ASTNode block) {
        SourceFile sourceFile = (SourceFile) pop();
        sourceFile.blocks.add(block);
        return push(sourceFile); }

    /**
     * Pop off a DocBlock, add the given Directive or DocText and push the
     * DocBlock back onto the stack.
     */
    boolean addToDocBlock(ASTNode an) {
        DocBlock docBlock = (DocBlock) pop();
        if (an instanceof Directive) {
            docBlock.directives.add((Directive) an); }
        else if (an instanceof DocText) {
            docBlock.docTexts.add((DocText) an); }
        else { throw new IllegalStateException(); }
        return push(docBlock); }
        
    boolean addToCodeBlock(String line) {
        CodeBlock codeBlock = (CodeBlock) pop();
        codeBlock.lines.put(curLineNum - 1, line);
        return push(codeBlock); }

    boolean addToDocText(String line) {
        DocText docText = (DocText) pop();
        docText.value += line;
        return push(docText); }

    boolean printValueStack() {
        for (int i = 0; i < getContext().getValueStack().size(); i++) {
            System.out.println(i + ": " + peek(i)); }
        return true; }

}
