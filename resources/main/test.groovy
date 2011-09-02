import com.jdblabs.jlp.*
import org.parboiled.Parboiled
import org.parboiled.parserunners.ReportingParseRunner
import org.parboiled.parserunners.RecoveringParseRunner

makeParser = {
    println "Making the standard parser."
    println "---------------------------"

    parser = Parboiled.createParser(JLPPegParser.class)
    parseRunner = new ReportingParseRunner(parser.SourceFile())
}

makeExperimentalParser = {
    println "Making the experimental parser."
    println "-------------------------------"

    parser = Parboiled.createParser(com.jdblabs.jlp.experimental.JLPPegParser.class)
    parseRunner = new ReportingParseRunner(parser.SourceFile())
}

simpleTest = {
    println "Parsing the simple test into 'result'."
    println "--------------------------------------"

    testLine = """%% This the first test line.
    %% Second Line
    %% Third Line \n\n Fifth line \n\n %% Seventh line \n\n
    %% @author Eigth Line
    %% @Example Ninth Line
    %%    Markdown lines (tenth line)
    %%    Still markdown (eleventh line)
    Twelfth line is a code line"""

    simpleResult = parseRunner.run(testLine)
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
