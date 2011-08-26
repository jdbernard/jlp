import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;

@BuildParseTree
public class TestParser extends BaseParser<Object> {

    public Rule S() {
        return OneOrMore(
            Sequence(A(), OneOrMore(B()), C())); }

    Rule A() { return Ch('a'); }
    Rule B() { return Ch('b'); }
    Rule C() { return Ch('c'); }

    public static void main(String[] args) {
        TestParser parser = Parboiled.createParser(TestParser.class);
        ReportingParseRunner parseRunner = new ReportingParseRunner(parser.S());

        ParsingResult result = parseRunner.run("abbbbcabc");
        System.out.println(result.matched ?  printNodeTree(result) + "\n" : "No Match");
    }
}
