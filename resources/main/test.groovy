import com.jdblabs.jlp.JLPPegParser
import org.parboiled.Parboiled
import org.parboiled.parserunners.ReportingParseRunner
import org.parboiled.parserunners.RecoveringParseRunner

parser = Parboiled.createParser(JLPPegParser.class)
parseRunner = new RecoveringParseRunner(parser.SourceFile())

testLine = """%% This the first test line.
%% Second Line
%% Third Line
Fourth line
%% Fifth line
%% @author Sixth Line
%% @Example Seventh Line
%%    Markdown lines (eigth line)
%%    Still markdown (ninth line)
Tenth line is a code line
"""

result = parseRunner.run(testLine)
