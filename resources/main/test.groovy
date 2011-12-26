import com.jdblabs.jlp.*
import org.parboiled.Parboiled
import org.parboiled.parserunners.*

"Making the standard parser."
"---------------------------"

jp = Parboiled.createParser(JLPPegParser.class)
ep = Parboiled.createParser(JLPPegParser, '%%')
jrpr = new ReportingParseRunner(jp.SourceFile())
jtpr = new TracingParseRunner(jp.SourceFile())
erpr = new ReportingParseRunner(ep.SourceFile())
etpr = new TracingParseRunner(ep.SourceFile())

vbsFile = new File('vbs_db_records.hrl')
javaFile = new File('Test.java')
docsDir = new File('jlp-docs')
docsDir.mkdirs()

simpleTest = { parseRunner ->

    println "Parsing the simple test into 'result'."
    println "--------------------------------------"

    testLine = """%% This the first test line.
    %% Second Line
    Actual third line that screws stuff up.
    %% Third Line \n\n Fifth line \n\n %% Seventh line \n\n
    %% @author Eigth Line
    %% @Example Ninth Line
    %%    Markdown lines (tenth line)
    %%    Still markdown (eleventh line)
    Twelfth line is a code line"""

    simpleResult = parseRunner.run(testLine)
}

vbsTest = { parseRunner ->
    println "Parsing vbs_db_records.hrl into 'vbsResult'."
    println "--------------------------------------------"

    println "vbsFile is ${vbsFile.exists() ? 'present' : 'absent'}."
    vbsTestInput = vbsFile.text

    vbsParsed = parseRunner.run(vbsTestInput)

    /*vbsResult = LiterateMarkdownGenerator.generateDocuments(["vbs_db_records.hrl": vbsParsed.resultValue])."vbs_db_records.hrl"

    println "Writing to file 'vbs_db_records.html'."
    println "--------------------------------------"

    (new File('vbs_db_records.html')).withWriter { out -> out.println vbsResult }

    return [vbsParsed, vbsResult]*/
    return vbsParsed
}

javaTest = { parseRunner ->
    println "Parsing Test.java into 'javaResult'."
    println "------------------------------------"

    println "javaFile is ${javaFile.exists() ? 'present' : 'absent'}."
    javaTestInput = javaFile.text

    javaParsed = parseRunner.run(javaTestInput)
    javaSF = javaParsed.valueStack.peek()

    return [javaParsed: javaParsed, javaSF: javaSF]
}
