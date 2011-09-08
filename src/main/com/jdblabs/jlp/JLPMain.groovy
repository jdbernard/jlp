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
        cli._(longOpt: 'relative-path-root', args: 1, required: false,
            'Resolve all relative paths against this root.')

        // parse options
        def opts = cli.parse(args)

        // display help if requested
        if (opts.h) {
            cli.usage()
            return }

        // get the relative path root (or set to current directory if not given)
        def pathRoot = new File(opts."relative-path-root" ?: ".")

        // fail if our root is non-existant
        if (!pathRoot.exists() || !pathRoot.isDirectory()) {
            System.err.println "'${pathRoot.path}' is not a valid directory."
            System.exit(1) }

        // get the output directory and create it if necessary
        def outputDir = opts.o ? new File(opts.o) : new File("jlp-docs")

        // resolve the output directory against our relative root
        if (!outputDir.isAbsolute()) {
            outputDir = new File(pathRoot, outputDir.path) }

        // create the output directory if it does not exist
        if (!outputDir.exists()) outputDir.mkdirs()

        // get the CSS theme to use
        def css = JLPMain.class.getResourceAsStream("/jlp.css") // TODO: make an option
        css = css.text

        // get files passed in
        def filenames = opts.getArgs()
        
        // parse input
        Map parsedFiles = filenames.inject([:]) { acc, filename ->

            // create the File object
            File file = new File(filename)

            // if this is a relative path, resolve it against our root path
            if (!file.isAbsolute()) { file = new File(pathRoot, filename) }

            // parse the file, store the result
            acc[filename] = inst.parse(file)
            return acc }

        // generate output
        Map htmlDocs = LiterateMarkdownGenerator.generateDocuments(parsedFiles)

        // write output files
        htmlDocs.each { filename, html ->

            // split the path into parts
            def fileParts = filename.split(/[\.\/]/)

            // default the subdirectory to the output directory
            File subDir = outputDir

            // if the input file was in a subdirectory, we want to mirror that
            // structure here.
            if (fileParts.length > 2) {

                // find the relative subdirectory of this file
                subDir = new File(outputDir, fileParts[0..-3].join('/'))

                // create that directory if needed
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
