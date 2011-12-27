/**
 * @author Jonathan Bernard
 * @copyright JDB Labs 2010-2011
 */
package com.jdblabs.jlp

import com.jdblabs.jlp.ast.ASTNode
import com.jdblabs.jlp.ast.SourceFile
import org.parboiled.Parboiled
import org.parboiled.parserunners.ReportingParseRunner

/**
 * @api JLPMain is the entrypoint for the system. It is responsible for parsing
 * the command-line options and invoking the Processor.
 * @org jlp.jdb-labs.com/JLPMain
 */
public class JLPMain {

    public static void main(String[] args) {

        /// #### Define command-line options.
        /// We are using the Groovy wrapper around the Apache Commons CLI
        /// library.
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

        /// #### Parse the options.
        def opts = cli.parse(args)

        /// Display help if requested.
        if (opts.h) {
            cli.usage()
            return }

        /// Get the relative path root (or set to current directory if it was
        /// not given)
        def pathRoot = new File(opts."relative-path-root" ?: ".")

        /// If our root is non-existant we will print an error and exit.. This
        /// is possible if a relative path root was passed as an option.
        if (!pathRoot.exists() || !pathRoot.isDirectory()) {
            System.err.println "'${pathRoot.path}' is not a valid directory."
            System.exit(1) }

        /// Get the output directory, either from the command line or by
        /// default.
        def outputDir = opts.o ? new File(opts.o) : new File("jlp-docs")

        /// Resolve the output directory against our relative root
        if (!outputDir.isAbsolute()) {
            outputDir = new File(pathRoot, outputDir.path) }

        /// Create the output directory if it does not exist.
        if (!outputDir.exists()) outputDir.mkdirs()

        /// Get the CSS theme to use. We will start by assuming the default will
        /// be used.
        def css = JLPMain.class.getResourceAsStream("/jlp.css")

        /// If the CSS file was specified on the command-line, let's look for it.
        if (opts.'css-file') {
            def cssFile = new File(opts.'css-file')

            /// Resolve the file against our relative root.
            if (!cssFile.isAbsolute()) {
                cssFile = new File(pathRoot, cssFile.path) }
                
            /// Finally, make sure the CSS file actually exists.
            if (cssFile.exists()) { css = cssFile }

            /// If it does not, we are going to warn the user and keep the
            /// default.
            else {
                println "WARN: Could not fine the custom CSS file: '" +
                    "${cssFile.canonicalPath}'."
                println "      Using the default CSS." }}

        /// Extract the text from our css source (either an InputStream or a
        /// File)
        css = css.text

        /// #### Create the input file list.

        /// We will start with the filenames passed as arguments on the command
        /// line.
        def filenames = opts.getArgs()
        def inputFiles = []

        filenames.each { filename ->

            /// For each filename we try to resolve it to an actual file
            /// relative to our root.
            File file = new File(filename)
            if (!file.isAbsolute()) { file = new File(pathRoot, filename) } 

            /// If this file does not exist, warn the user and skip it.
            if (!file.exists()) {
                System.err.println(
                    "'${file.canonicalPath}' does not exist: ignored.")
                return }
                
            /// If this file is a directory, we want to add all the files in it
            /// to our input list, recursing into all the subdirectories and
            /// adding their files as well.
            if (file.isDirectory()) { file.eachFileRecurse {
                if (it.isFile()) { inputFiles << it }}}

            /// Not a directory, just add the file.
            else { inputFiles << file } }

        /// #### Process the files.
        Processor.process(outputDir, css, inputFiles)
    }

}
