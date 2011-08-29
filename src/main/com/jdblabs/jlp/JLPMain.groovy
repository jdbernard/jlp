package com.jdblabs.jlp

import com.jdblabs.jlp.ast.ASTNode
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
        Map parsedFiles = files.inject([:]) { acc, file ->
            def parsed = inst.parse(new File(file))
            acc[file.canonicalPath] = parsed
            return acc }

        // -------- generate output -------- //
    }

    public JLPMain() {
        parser = Parboiled.createParser(JLPPegParser.class)
    }

    public Map parse(File inputFile) {
        def parseRunner = new ReportingParseRunner(parser.SourceFile())

        // parse the file
        return parseRunner.run(inputFile).resultValue
    }

    public def generate(def emitter, List<ASTNode> blocks) {
        // second pass, semantics
    }
}
