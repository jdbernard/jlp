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
        cli._('Use <css-file> for the documentation css.',
            longOpt: 'css-file', args: 1, required: false, argName: 'css-file')
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

        // get the CSS theme to use. We will start by assuming the default will
        // be used.
        def css = JLPMain.class.getResourceAsStream("/jlp.css")

        // If the CSS file was specified on the command-line, let's look for it.
        if (opts.'css-file') {
            def cssFile = new File(opts.'css-file')
            // resolve against our relative root
            if (!cssFile.isAbsolute()) {
                cssFile = new File(pathRoot, cssFile.path) }
                
            // Finally, make sure the file actually exists.
            if (cssFile.exists()) { css = cssFile }
            else {
                println "WARN: Could not fine the custom CSS file: '" +
                    "${cssFile.canonicalPath}'."
                println "      Using the default CSS." }}

        // Extract the text from our css source (either an InputStream or a
        // File)
        css = css.text

        // get files passed in
        def filenames = opts.getArgs()
        def inputFiles = []

        filenames.each { filename ->
            // create a File object
            File file = new File(filename)

            // if this is a relative path, resolve it against our path root
            if (!file.isAbsolute()) { file = new File(pathRoot, filename) } 

            // if this file does not exist, warn the user and skip it
            if (!file.exists()) {
                System.err.println(
                    "'${file.canonicalPath}' does not exist: ignored.")
                return }
                
            // if this file is a directory, add all the files in it (recurse
            // into sub-directories and add their contents as well).
            if (file.isDirectory()) { file.eachFileRecurse {
                if (it.isFile()) { inputFiles << it }}}

            else { inputFiles << file } }

        Processor.process(outputDir, css, inputFiles)
    }

}
