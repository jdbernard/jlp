package com.jdblabs.jlp.experimental;

import org.parboiled.Action;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.annotations.*;

@BuildParseTree
public class DeadSimple extends BaseParser {

    public Rule S() {
        return OneOrMore(FirstOf(Line(), EmptyLine()).skipNode()); }

    public Rule Line() {
        return Sequence(OneOrMore(NOT_EOL).skipNode(), EOL); }

    public Rule EmptyLine() {
        return EOL; }

    public Rule EOL = Ch('\n');
    public Rule NOT_EOL = Sequence(TestNot(EOL), ANY);
}
