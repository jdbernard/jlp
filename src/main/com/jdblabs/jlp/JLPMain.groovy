package com.jdblabs.jlp

import com.jdblabs.jlp.ast.ASTNode
import com.jdblabs.jlp.ast.SourceFile
import org.parboiled.Parboiled
import org.parboiled.parserunners.ReportingParseRunner

public class JLPMain {

    public static void main(String[] args) {

        // create command-line parser
        CliBuilder cli = new CliBuilder(
            usage: 'jlp [options] <src-file> <src-file> ...')

        // define options
        cli.h('Print this help information.', longOpt: 'help', required: false)
        cli.o("Output directory (defaults to 'jlp-docs').",
            longOpt: 'output-dir', args: 1, argName: "output-dir",
            required: false)
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
        def inputFiles = (filenames.collect { filename ->
            // create a File object
            File file = new File(filename) 
            
            // if this is a relative path, resolve it against our path root
            if (!file.isAbsolute()) { file = new File(pathRoot, filename) } 
            
            // warn the user about files that do not exist
            if (!file.exists())  {
                System.err.println
                    "'${file.canonicalPath}' does not exist: ignored." }

            return file }).findAll { it.exists() }

        Processor.process(outputDir, css, inputFiles)
    }

}
