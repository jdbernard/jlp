package com.jdblabs.jlp;

import com.jdblabs.jlp.ast.*;
import java.util.ArrayList;
import java.util.List;
import org.parboiled.Action;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.annotations.*;
import org.parboiled.parserunners.ReportingParseRunner;

/**
 * 
 * @org jlp.jdb-labs.com/JLPPegParser
 */
@BuildParseTree
public class JLPPegParser extends BaseParser<Object> implements JLPParser {

    int curLineNum = 1;

    public JLPPegParser(String mdocStart, String mdocEnd,
        String mdocLineStart, String sdocStart) {

        MDOC_START = String(mdocStart).label("MDOC_START");
        MDOC_END = String(mdocEnd).label("MDOC_END");
        SDOC_START = String(sdocStart).label("SDOC_START");
        MDOC_LINE_START = AnyOf(mdocLineStart).label("MDOC_LINE_START"); }

    public JLPPegParser(String sdocStart) {
        MDOC_START = NOTHING;
        MDOC_LINE_START = NOTHING;
        MDOC_END = NOTHING;
        SDOC_START = String(sdocStart).label("SDOC_START"); }

    public JLPPegParser() {
        this("/**", "*/", "!#$%^&*()_-=+|;:'\",<>?~`", "///"); }

    public SourceFile parse(String input) {
        ReportingParseRunner rpr = new ReportingParseRunner(this.SourceFile());
        return (SourceFile) rpr.run(input).resultValue; }

    /**
     * Parses the rule:
     *
     *     SourceFile = (Block / DocBlock / CodeBlock)+
     *
     * Pushes a SourceFile node on the stack.
     */
    public Rule SourceFile() {
        return Sequence(
            /// At the start of processing a new SourceFile, we need to set up
            /// our internal state. 

            /// Clear the line count.
            clearLineCount(),

            /// Add the top-level SourceFile AST node.
            push(new SourceFile()),

            /// A SourceFile is made up of one or more Blocks
            OneOrMore(Sequence(
                /// All of these options result in one new Block pushed onto the
                /// stack.
                FirstOf(

                    /// Match a whole Block. This pushes a Block on the stack.
                    Block(),    

                    /// A standalone DocBlock. We will create an empty CodeBlock
                    /// to pair with it, then push a new Block onto the stack
                    /// made from the DocBlock and the empty CodeBlock
                    Sequence(
                        /// 1. We need to remember the line number to create the
                        ///    Block
                        push(curLineNum),

                        /// 2. Match the DocBlock.
                        DocBlock(),

                        /// 3. Create the empty CodeBlock.
                        push(new CodeBlock(curLineNum)),

                        /// 4. Create the Block and push it onto the stack.
                        push(new Block((CodeBlock) pop(), (DocBlock) pop(),
                            popAsInt()))),

                    /// A standalone CodeBlock. Similar to the standalone
                    /// DocBlock, we will create an empty DocBlock to pair with
                    /// the CodeBlock to make a Block, which is pushed onto the
                    /// stack:
                    ///
                    /// *Note: With the way the parser is currently written,
                    ///        this will only match a CodeBlock that occurs
                    ///        before any DocBlock.*
                    Sequence(
                        /// 1. Remember the line number for the Block.
                        push(curLineNum),

                        /// 2. Create the empty DocBlock.
                        push(new DocBlock(curLineNum)),

                        /// 3. Match the CodeBlock
                        CodeBlock(),

                        /// 4. Create the Block and push it onto the stack
                        push(new Block((CodeBlock) pop(), (DocBlock) pop(),
                            popAsInt())))),

                /// pop the Block created by one of the above options and add it
                /// to the SourceFile
                addBlockToSourceFile((Block) pop())))); }

    /**
     * Parses the rule:
     *
     *     Block = DocBlock CodeBlock
     *
     * Pushes a Block onto the stack
     */
    Rule Block() {
        return Sequence(
            push(curLineNum),
            DocBlock(), CodeBlock(),

            /// A DocBlock and a CodeBlock are pushed onto the stack by the
            /// above rules. Pop them off, along with the line number we pushed
            /// before that, and create a new Block node.
            push(new Block((CodeBlock) pop(), (DocBlock) pop(), popAsInt()))); }

    /**
     * Parses the rule:
     *
     *     DocBlock = SDocBlock / MDocBlock
     *
     * Pushes a DocBlock onto the stack.
     */
    Rule DocBlock() { return FirstOf(SDocBlock(), MDocBlock()); }

    /**
     * Parses the rule:
     *
     *     SDocBlock = (SDirective / SDocText)+
     *
     * Pushes a DocBlock object onto the stack
     */
    Rule SDocBlock() {
        return Sequence(
            push(new DocBlock(curLineNum)),
            OneOrMore(Sequence(
                FirstOf(SDirective(), SDocText()),
                addToDocBlock((ASTNode) pop())))); }

    /**
     * Parses the rule:
     *
     *     MDocBlock = MDOC_START (MDirective / MDocText)+ MDOC_END
     *
     * Pushes a DocBlock object onto the stack
     */
    Rule MDocBlock() {
        return Sequence(
            push(new DocBlock(curLineNum)),
            MDOC_START,
            ZeroOrMore(Sequence(
                /// We need to be careful to exclude MDOC_END here, as there can
                /// be some confusion otherwise between the start of a line with
                /// MDOC_LINE_START and MDOC_END depending on what values the
                /// user has chosen for them
                TestNot(MDOC_END), FirstOf(MDirective(), MDocText()),
                addToDocBlock((ASTNode) pop()))),
            MDOC_END); }
    /**
     * Parses the rule:
     *
     *     CodeBlock = (RemainingCodeLine)+
     *
     * Pushes a CodeBlock onto the stack.
     */
    Rule CodeBlock() {
        return Sequence(
            push(new CodeBlock(curLineNum)),
            OneOrMore(Sequence(RemainingCodeLine(),
                addToCodeBlock(match())))); }

    /**
     * Parses the rule:
     *
     *     SDirective = SDocLineStart AT (SLongDirective / SShortDirective)
     *
     * Pushes a Directive node on the stack.
     */
    Rule SDirective() {
        return Sequence(
            SDocLineStart(), AT, FirstOf(SLongDirective(), SShortDirective())); }

    /**
     * Parses the rule:
     *
     *     MDirective = MDocLineStart? AT (MLongDirective / MShortDirective)
     *
     * Pushes a Directive node onto the stack.
     */
    Rule MDirective() {
        return Sequence(
            Optional(MDocLineStart()),
            AT, FirstOf(MLongDirective(), MShortDirective())); }

    /**
     * Parses the rule:
     *
     *     SLongDirective =
     *      (API_DIR / EXAMPLE_DIR) RemainingSDocLine SDocText?
     *
     * Pushes a Directive node onto the stack.
     */
    Rule SLongDirective() {
        return Sequence(
            push(curLineNum),

            FirstOf(API_DIR, EXAMPLE_DIR),  push(match()),
            RemainingSDocLine(),            push(match()),

            Optional(Sequence(
                SDocText(),
                swap(),
                push(popAsString() + ((DocText) pop()).value))),
                
            push(new Directive(popAsString(), popAsString(), popAsInt()))); }

    /**
     * Parses the rule:
     *
     *     MLongDirective = 
     *      (API_DIR / EXAMPLE_DIR) RemainingMDocLine MDocText?
     *
     * Pushes a Directive node onto the stack.
     */
    Rule MLongDirective() {
        return Sequence(
            push(curLineNum),

            FirstOf(API_DIR, EXAMPLE_DIR), push(match()),
            RemainingMDocLine(),           push(match()),

            Optional(Sequence(
                MDocText(),
                swap(),
                push(popAsString() + ((DocText) pop()).value))),

            push(new Directive(popAsString(), popAsString(), popAsInt()))); }

    /**
     * Parses the rule:
     *
     *     SShortDirective = (AUTHOR_DIR / ORG_DIR / COPYRIGHT_DIR) RemainingSDocLine
     *
     * Pushes a Directive node onto the stack.
     */
    Rule SShortDirective() {
        return Sequence(
            push(curLineNum),
            FirstOf(AUTHOR_DIR, ORG_DIR, COPYRIGHT_DIR), push(match()),
            RemainingSDocLine(),
            
            push(new Directive(match().trim(), popAsString(), popAsInt()))); }

    /**
     * Parses the rule:
     *
     *     MShortDirective = (AUTHOR_DIR / ORG_DIR / COPYRIGHT_DIR) RemainingMDocLine
     *
     * Pushes a Directive node onto the stack.
     */
    Rule MShortDirective() {
        return Sequence(
            push(curLineNum),
            FirstOf(AUTHOR_DIR, ORG_DIR, COPYRIGHT_DIR), push(match()),
            RemainingMDocLine(),

            push(new Directive(match().trim(), popAsString(), popAsInt()))); }

    /**
     * Parses the rule:
     *
     *     SDocText = (SDocLineStart !AT RemainingSDocLine)+
     *
     * Pushes a DocText node onto the stack.
     */
    Rule SDocText() {
        return Sequence(
            push(new DocText(curLineNum)),
            OneOrMore(Sequence(
                SDocLineStart(), TestNot(AT), RemainingSDocLine(),
                addToDocText(match())))); }

    /**
     * Parses the rule:
     *
     *     MDocText = (MDocLineStart? !AT RemainingMDocLine)+
     *
     * Pushes a DocText node onto the stack.
     */
    Rule MDocText() {
        return Sequence(
            push(new DocText(curLineNum)),
            OneOrMore(Sequence(
                Optional(MDocLineStart()),
                TestNot(AT), RemainingMDocLine(),
                addToDocText(match())))); }

    /**
     * Parses the rule:
     *
     *     SDocLineStart = SPACE* SDOC_START SPACE?
     */
    Rule SDocLineStart() {
        return Sequence(
            ZeroOrMore(SPACE), SDOC_START, Optional(SPACE)); }

    /**
     * Parses the rule:
     *
     *     MDocLineStart = SPACE* !MDOC_END MDOC_LINE_START SPACE?
     */
    Rule MDocLineStart() {
        return Sequence(
            ZeroOrMore(SPACE), TestNot(MDOC_END), MDOC_LINE_START, Optional(SPACE)); }

    /**
     * Parses the rule:
     *
     *     RemainingSDocLine = ((!EOL)* EOL) / ((!EOL)+ EOI)
     */
    Rule RemainingSDocLine() {
        return FirstOf(
            Sequence(ZeroOrMore(NOT_EOL), EOL, incLineCount()),
            Sequence(OneOrMore(NOT_EOL), EOI, incLineCount())); }

    /**
     * Parses the rule:
     *
     *     RemainingMDocLine = 
     *      ((!(EOL / MDOC_END))* EOL) /
     *      ((!MDOC_END)+)
     */
    Rule RemainingMDocLine() {
        return FirstOf(
            /// End of line, still within the an M-style comment block
            Sequence(
                ZeroOrMore(Sequence(TestNot(FirstOf(EOL, MDOC_END)), ANY)),
                EOL,
                incLineCount()),

            /// End of M-style comment block
            OneOrMore(Sequence(TestNot(MDOC_END), ANY))); }

    /**
     * Parses the rule:
     *
     *     RemainingCodeLine = 
     *      ((!(EOL / MDOC_START / SDocLineStart))* EOL) /
     *      (!(MDOC_START / SDocLineStart))+
     */
    Rule RemainingCodeLine() {
        return FirstOf(
            /// End of line, still within the code block.
            Sequence(
                ZeroOrMore(Sequence(
                    TestNot(FirstOf(EOL, MDOC_START, SDocLineStart())),
                    ANY)),
                EOL,
                incLineCount()),

            /// Found an MDOC_START or SDocLineStart
            OneOrMore(Sequence(TestNot(FirstOf(MDOC_START, SDocLineStart())), ANY))); }

    Rule AT         = Ch('@').label("AT");
    Rule EOL        = FirstOf(String("\r\n"), Ch('\n'), Ch('\r')).label("EOL");
    Rule NOT_EOL    = Sequence(TestNot(EOL), ANY).label("NOT_EOL");
    Rule SPACE      = AnyOf(" \t").label("SPACE");

    /// Configurable
    Rule MDOC_START;
    Rule MDOC_END;
    Rule MDOC_LINE_START;
    Rule SDOC_START;

    /// directive terminals
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
