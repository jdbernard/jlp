import com.jdblabs.jlp.*
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

    %% Sixth line
    %% @author Seventh Line
    %% @Example Eigth Line
    %%    Markdown lines (ninth line)
    %%    Still markdown (tenth line)
    Eleventh line is a code line
    """

    parseRunner.run(testLine)
}

vbsTest = {
    println "Parsing vbs_db_records.hrl into 'vbsResult'."
    println "--------------------------------------------"

    vbsTestFile = new File('vbs_db_records.hrl')
    println "vbsTestFile is ${vbsTestFile.exists() ? 'present' : 'absent'}."
    vbsTestInput = vbsTestFile.text

    vbsParsed = parseRunner.run(vbsTestInput)

    vbsResult = MarkdownGenerator.generateDocuments([vbs: vbsParsed.resultValue]).vbs

    println "Writing to file 'vbs_result.html'."
    println "----------------------------------"

    (new File('vbs_result.html')).withWriter { out -> out.println vbsResult }

    return [vbsParsed, vbsResult]
}
