package com.jdblabs.jlp

import com.jdblabs.jlp.ast.ASTNode
import com.jdblabs.jlp.ast.SourceFile
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
        cli.o("Output directory (defaults to 'jlp-docs').",
            longOpt: 'output-dir', required: false)

        // parse options
        def opts = cli.parse(args)

        // display help if requested
        if (opts.h) {
            cli.usage()
            return }

        // get the output directory and create it if necessary
        def outputDir = opts.o ? new File(opts.o) : new File("jlp-docs")
        if (!outputDir.exists()) outputDir.mkdirs()

        // get the CSS theme to use
        def css = JLPMain.class.getResourceAsStream("/jlp.css") // TODO: make an option
        css = css.text

        // get files passed in
        def filenames = opts.getArgs()
        
        // -------- parse input -------- //
        Map parsedFiles = filenames.inject([:]) { acc, filename ->
            acc[filename] = inst.parse(new File(filename))
            return acc }

        // -------- generate output -------- //
        Map htmlDocs = LiterateMarkdownGenerator.generateDocuments(parsedFiles)

        // -------- write output files ------- //
        htmlDocs.each { filename, html ->
            // split the path into parts
            def fileParts = filename.split(/[\.\/]/)

            File subDir = new File('.')

            if (fileParts.length > 2) {
                // find the relative subdirectory of this file
                subDir = new File(outputDir, fileParts[0..-3].join('/'))
                if (!subDir.exists()) subDir.mkdirs()
            }

            // recreate the output filename
            def outputFilename = fileParts[-2] + ".html"

            // write the HTML to the file
            new File(subDir, outputFilename).withWriter { fileOut ->

                // write the CSS if it is not present
                File cssFile = new File(subDir, "jlp.css")
                if (!cssFile.exists()) cssFile.withWriter { cssOut ->
                    cssOut.println css }


                // write the file
                fileOut.println html } }
    }

    public JLPMain() {
        parser = Parboiled.createParser(JLPPegParser.class)
    }

    public SourceFile parse(File inputFile) {
        def parseRunner = new ReportingParseRunner(parser.SourceFile())

        // parse the file
        return parseRunner.run(inputFile.text).resultValue
    }
}
