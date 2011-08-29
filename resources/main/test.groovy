import com.jdblabs.jlp.EchoEmitter
import com.jdblabs.jlp.JLPPegParser
import org.parboiled.Parboiled
import org.parboiled.parserunners.ReportingParseRunner
import org.parboiled.parserunners.RecoveringParseRunner

parser = Parboiled.createParser(JLPPegParser.class)
parseRunner = new RecoveringParseRunner(parser.SourceFile())


simpleTest = {
    "Parsing the simple test into 'result'.\n" +
    "--------------------------------------\n"

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

    parseRunner.run(testLine)
}

vbsTest = {
    "Parsing vbs_db_records.hrl into 'vbsResult'."
    "--------------------------------------------\n"

    vbsTestFile = new File('vbs_db_records.hrl')
    println "vbsTestFile is ${vbsTestFile.exists() ? 'present' : 'absent'}."
    vbsTestInput = vbsTestFile.text

    parseRunner.run(vbsTestInput)
}
