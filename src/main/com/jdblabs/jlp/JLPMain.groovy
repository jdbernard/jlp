package com.jdblabs.jlp

import org.parboiled.Parboiled
import org.parboiled.parserunners.ReportingParseRunner

public class JLPMain {

    private JLPPegParser parser

    public static void main(String[] args) {

        JLPMain inst = new JLPMain()

        // create command-line parser
        CliBuilder cli = new CliBuilder(
            usage: 'jlp [options] <src-file> <src-file> ...')

        // define options
        cli.h('Print this help information.', longOpt: 'help', required: false)

        // parse options
        def opts = cli.parse(args)

        // display help if requested
        if (opts.h) {
            cli.usage()
            return }

        // get files passed in
        def filenames = opts.getArgs()
        def files = filenames.collect { new File(it) }
        
        // -------- parse input -------- //
        Map parsed = files.inject([:]) { docContext, file ->
            inst.parse(new File(file), docContext) }

        // -------- generate output -------- //
    }

    public JLPMain() {
        parser = Parboiled.createParser(JLPPegParser.class)
    }

    public Map parse(File inputFile, Map docCtx) {
        def parseRunner = new ReportingParseRunner(parser.SourceFile())

        // parse the file
        def firstPass = parseRunner.run(inputFile)
    }

}
