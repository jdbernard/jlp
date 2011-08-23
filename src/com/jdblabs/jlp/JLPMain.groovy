package com.jdblabs.jlp

public class JLPMain {

    public static void main(String args[]) {

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

        Map documentContext = [ docs: [:] ]

        // get files passed in
        def filenames = opts.getArgs()
        def files = filenames.collect { new File(if) }
        
        // -------- parse input -------- //
        files.inject(documentContext) { docContext, file ->
            inst.parse(new File(file), docContext) }

        // -------- generate output -------- //
    }

    public void parse(File inputFile, Map docCtx) {
        def currentDocBlock
        def thisDoc = [ blocks:[] ]

        String docName = inputFile.name.substring(
            0, inputFile.name.lastIndexOf('.'))

        docCtx.docs[docName] = thisDoc

        inputFile.eachLine { line, lineNum ->
            
        }
    }

}
