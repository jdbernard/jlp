/**
 * # JLPMain
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright 2011-2012 [JDB Labs LLC](http://jdb-labs.com)
 */
package com.jdblabs.jlp

import com.jdblabs.jlp.ast.ASTNode
import com.jdblabs.jlp.ast.SourceFile
import org.parboiled.Parboiled
import org.parboiled.parserunners.ReportingParseRunner
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @api JLPMain is the entrypoint for the system. It is responsible for parsing
 * the command-line options and invoking the Processor.
 * @org jlp.jdb-labs.com/JLPMain
 */
public class JLPMain {

    public static final String VERSION = "1.10"

    private static Logger log = LoggerFactory.getLogger(JLPMain.class)

    public static void main(String[] args) {

        /// #### Define command-line options.
        /// We are using Groovy's [CliBuilder] (a wrapper around the Apache
        /// Commons library).
        ///
        /// [CliBuilder]: http://groovy.codehaus.org/gapi/groovy/util/CliBuilder.html
        CliBuilder cli = new CliBuilder(
            usage: 'jlp [options] <src-file> <src-file> ...')

        /// -h, --help
        /// :   Print help information.
        cli.h('Print this help information.', longOpt: 'help', required: false)

        /// -o, --outputdir <output-directory>
        /// :   Set the output directory where the documentation will be
        ///     written.
        cli.o("Output directory (defaults to 'jlp-docs').",
            longOpt: 'output-dir', args: 1, argName: "output-dir",
            required: false)

        /// --css-file <file>
        /// :   Specify an alternate CSS file for the output documentation.
        cli._('Use <css-file> for the documentation css.',
            longOpt: 'css-file', args: 1, required: false, argName: 'css-file')

        /// --relative-path-root <root-directory>
        /// :   Override the current working directory. This is useful if you
        ///     are invoking jlp remotely, or if the current working directory
        ///     is incorrectly set by the executing environment.
        cli._(longOpt: 'relative-path-root', args: 1, required: false,
            'Resolve all relative paths against this root.')

        /// --version
        /// :   Display JLP versioning information.
        cli._(longOpt: 'version', 'Display the JLP version information.')

        /// --no-source
        /// :   Do not copy the source files into the output directory alongside
        ///     the documentation.
        cli._(longOpt: 'no-source', 'Do not copy the source files into the' +
            ' output directory alongside the documentation.')

        /// #### Parse the options.
        def opts = cli.parse(args)

        /// Display help and version information if requested.
        if (opts.h) {
            cli.usage()
            return }

        if (opts.version) {
            println "JLP v$VERSION"
            return }

        /// Get the relative path root (or set to current directory if it was
        /// not given)
        def pathRoot = new File(opts."relative-path-root" ?: ".")
        log.debug("Relative path root: '{}'.", pathRoot.canonicalPath)

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

        log.debug("Output directory: '{}'.", outputDir.canonicalPath)

        /// Get the CSS theme to use. We will start by assuming the default will
        /// be used.
        def css = JLPMain.class.getResourceAsStream("/css/jlp.css")

        /// If the CSS file was specified on the command-line, let's look for it.
        if (opts.'css-file') {
            def cssFile = new File(opts.'css-file')

            /// Resolve the file against our relative root.
            if (!cssFile.isAbsolute()) {
                cssFile = new File(pathRoot, cssFile.path) }

            /// Finally, make sure the CSS file actually exists.
            if (cssFile.exists()) {
                css = cssFile
                log.debug("Loading CSS from this file: '{}'.",
                    cssFile.canonicalPath) }

            /// If it does not, we are going to warn the user and keep the
            /// default.
            else {
                println "WARN: Could not fine the custom CSS file: '" +
                    "${cssFile.canonicalPath}'."
                println "      Using the default CSS." }}

        /// Look for our `--no-source` option.
        def includeSource = !opts."no-source"

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
            /// adding their files as well. We will ignore hidden files.
            if (file.isDirectory()) { file.eachFileRecurse {
                if (it.isFile() && !it.isHidden()) { inputFiles << it }}}

            /// Not a directory, just add the file.
            else { inputFiles << file } }

        /// #### Process the files.
        log.trace("Starting JLP processor.")
        Processor.process(outputDir, css, inputFiles, includeSource)
    }

}
