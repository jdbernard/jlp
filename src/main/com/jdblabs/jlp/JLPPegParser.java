package com.jdblabs.jlp;

import com.jdblabs.jlp.ast.*;
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
            // At the start of processing a new SourceFile, we need to set up
            // our internal state. 

            // Clear the line count.
            clearLineCount(),

            // Add the top-level SourceFile AST node.
            push(new SourceFile()),

            // A SourceFile is made up of one or more Blocks
            OneOrMore(Sequence(
                // All of these options result in one new Block pushed onto the
                // stack.
                FirstOf(

                    // Match a whole Block. This pushes a Block on the stack.
                    Block(),    

                    // A standalone DocBlock. We will create an empty CodeBlock
                    // to pair with it, then push a new Block onto the stack
                    // made from the DocBlock and the empty CodeBlock
                    Sequence(
                        // 1. We need to remember the line number to create the
                        // Block
                        push(curLineNum),

                        // 2. Match the DocBlock.
                        DocBlock(),

                        // 3. Create the empty CodeBlock.
                        push(new CodeBlock(curLineNum)),

                        // 4. Create the Block and push it onto the stack.
                        push(new Block((CodeBlock) pop(), (DocBlock) pop(),
                            popAsInt()))),

                    // A standalone CodeBlock. Similar to the standalone
                    // DocBlock, we will create an empty DocBlock to pair with
                    // the CodeBlock to make a Block, which is pushed onto the
                    // stack:
                    //
                    // *Note: With the way the parser is currently written,
                    //        this will only match a CodeBlock that occurs
                    //        before any DocBlock.*
                    Sequence(
                        // 1. Remember the line number for the Block.
                        push(curLineNum),

                        // 2. Create the empty DocBlock.
                        push(new DocBlock(curLineNum)),

                        // 3. Match the CodeBlock
                        CodeBlock(),

                        // 4. Create the Block and push it onto the stack
                        push(new Block((CodeBlock) pop(), (DocBlock) pop(),
                            popAsInt())))),

                // pop the Block created by one of the above options and add it
                // to the SourceFile
                addBlockToSourceFile((Block) pop())))); }

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
     *  Directive = DocLineStart AT (LongDirective / ShortDirective)
     *
     * Pushes a Directive node on the stack.
     */
    Rule Directive() {
        return Sequence(
            DocLineStart(), AT, FirstOf(LongDirective(), ShortDirective())); }

    /**
     * Parses the rule:
     *  LongDirective =
     *      (API_DIR / EXAMPLE_DIR) RemainingLine DocText?
     *
     * Pushes a Directive node onto the stack.
     */
    Rule LongDirective() {
        return Sequence(
            push(curLineNum),

            FirstOf(API_DIR, EXAMPLE_DIR),  push(match()),
            RemainingLine(),                            push(match()),

            Optional(Sequence(
                DocText(),
                swap(),
                push(popAsString() + ((DocText) pop()).value))),
                
            push(new Directive(popAsString(), popAsString(), popAsInt()))); }

    /**
     * Parses the rule:
     *  ShortDirective = (AUTHOR_DIR / ORG_DIR / COPYRIGHT_DIR) RemainingLine
     *
     * Pushes a Directive node onto the stack.
     */
    Rule ShortDirective() {
        return Sequence(
            push(curLineNum),
            FirstOf(AUTHOR_DIR, ORG_DIR, COPYRIGHT_DIR), push(match()),
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

    Rule DocLineStart() {
        return Sequence(
            ZeroOrMore(SPACE), DOC_LINE_START, Optional(SPACE)); }

    Rule NonEmptyLine() {
        return Sequence(OneOrMore(NOT_EOL), FirstOf(EOL, EOI)); }

    Rule RemainingLine() {
        return FirstOf(
            Sequence(ZeroOrMore(NOT_EOL), EOL, incLineCount()),
            Sequence(OneOrMore(NOT_EOL), EOI, incLineCount())); }

    Rule AT         = Ch('@').label("AT");
    Rule EOL        = FirstOf(String("\r\n"), Ch('\n'), Ch('\r')).label("EOL");
    Rule NOT_EOL    = Sequence(TestNot(EOL), ANY).label("NOT_EOL");
    Rule SPACE      = AnyOf(" \t").label("SPACE");
    Rule DOC_LINE_START = String("%%").label("DOC_LINE_START");

    // directive terminals
    Rule AUTHOR_DIR     = IgnoreCase("author");
    Rule COPYRIGHT_DIR  = IgnoreCase("copyright");
    Rule API_DIR        = IgnoreCase("api");
    Rule EXAMPLE_DIR    = IgnoreCase("example");
    Rule ORG_DIR        = IgnoreCase("org");

    String popAsString() { return (String) pop(); }

    Integer popAsInt() { return (Integer) pop(); }

    boolean clearLineCount() { curLineNum = 1; return true; }

    boolean incLineCount() { curLineNum++; return true; }

    boolean addBlockToSourceFile(Block block) {
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
