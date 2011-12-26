package com.jdblabs.jlp

import org.parboiled.BaseParser
import org.parboiled.Parboiled

/**
 * Processor processes one batch of input files to create a set of output files.
 * It holds the intermediate state needed by the generators and coordinates the
 * work of the parsers and generators for each of the input files.
 */
public class Processor {

    public Map<String, LinkAnchor> linkAnchors = [:]
    public Map<String, TargetDoc> docs = [:]
    public String currentDocId = null

    public File inputRoot
    public File outputRoot
    public String css

    // shortcut for docs[currentDocId]
    public TargetDoc currentDoc

    protected Map<String, JLPParser> parsers = [:]
    protected Map<String, JLPBaseGenerator> generators = [:]

    public static void process(File outputDir, String css,
    List<File> inputFiles) {

        // find the closest common parent folder to all of the files
        File inputDir = inputFiles.inject(inputFiles[0]) { commonRoot, file ->
            getCommonParent(commonRoot, file) }

        // create our processor instance
        Processor inst = new Processor(
            inputRoot: inputDir,
            outputRoot: outputDir,
            css: css)

        // run the process
        inst.process(inputFiles)
    }

    protected void process(inputFiles) {

        // Remember that the data for the processing run was initialized by the
        // constructor.

        inputFiles.each { file ->

            // set the current doc id
            def docId = getRelativeFilepath(inputRoot, file)

            // create the processing context for this file
            docs[docId] = new TargetDoc(sourceFile: file) }

        // Parse the input files.
        processDocs {
            
            // TODO: add logic to configure or autodetect the correct parser for
            // each file
            def parser = getParser(sourceTypeForFile(currentDoc.sourceFile))

            // TODO: error detection
            currentDoc.sourceAST = parser.parse(currentDoc.sourceFile.text) }

        // run our generator parse phase (first pass over the ASTs)
        processDocs {

            // TODO: add logic to configure or autodetect the correct generator
            // for each file
            def generator =
                getGenerator(sourceTypeForFile(currentDoc.sourceFile))
            generator.parse(currentDoc.sourceAST) }


        // Second pass by the generators, create output.
        processDocs {
            
            // TODO: add logic to configure or autodetect the correct generator
            // for each file
            def generator =
                getGenerator(sourceTypeForFile(currentDoc.sourceFile))
            currentDoc.output = generator.emit(currentDoc.sourceAST) }

        // Write the output to the output directory
        processDocs {
            
            // create the path to the output file
            String relativePath =
                getRelativeFilepath(inputRoot, currentDoc.sourceFile)

            File outputFile = new File(outputRoot, relativePath + ".html")
            File outputDir = outputFile.parentFile

            // create the directory if need be
            if (!outputDir.exists()) { outputDir.mkdirs() }

            // write the css file if it does not exist
            File cssFile = new File(outputDir, "jlp.css")
            if (!cssFile.exists()) { cssFile.withWriter{ it.println css } }

            // Copy the source file over
            (new File(outputRoot, relativePath)).withWriter {
                it.print currentDoc.sourceFile.text }

            // Write the output to the file.
            outputFile.withWriter { it.println currentDoc.output } } }

    protected def processDocs(Closure c) {
        docs.each { docId, doc ->
            currentDocId = docId
            currentDoc = doc

            return c() } }

    /**
     * Assuming our current directory is `root`, get the relative path to 
     * `file`.
     */
    public static String getRelativeFilepath(File root, File file) {
        // make sure our root is a directory
        if (!root.isDirectory()) root= root.parentFile

        def rootPath = root.canonicalPath.split('/')
        def filePath = file.canonicalPath.split('/')

        def relativePath = []

        // find the point of divergence in the two paths
        int i = 0
        while (i < Math.min(rootPath.length, filePath.length) &&
               rootPath[i] == filePath[i]) { i++ }

        // backtrack from our root to our common parent directory
        (i..<rootPath.length).each { relativePath << ".." }

        // add the path from our common parent directory to our file
        (i..<filePath.length).each { j -> relativePath << filePath[j] }

        return relativePath.join('/') }

    /**
     * Find the common parent directory to the given files.
     */ 
    public static File getCommonParent(File file1, File file2) {
            def path1 = file1.canonicalPath.split('/')
            def path2 = file2.canonicalPath.split('/')
            def newPath = []

            // build new commonPath based on matching paths so far
            int i = 0
            while (i < Math.min(path1.length, path2.length) &&
                   path1[i] == path2[i]) {
                
                newPath << path2[i]
                i++ }

            return new File(newPath.join('/')) }

    public static sourceTypeForFile(File sourceFile) {
        String extension
        def nameParts = sourceFile.name.split(/\./)

        if (nameParts.length == 1) { return 'binary' }
        else { extension = nameParts[-1] }

        switch (extension) {
            case 'c': case 'h': return 'c';
            case 'c++': case 'h++': case 'cpp': case 'hpp': return 'c++';
            case 'erl': case 'hrl': return 'erlang';
            case 'groovy': return 'groovy';
            case 'java': return 'java';
            case 'js': return 'javascript';
            case 'md': return 'markdown';
            default: return 'unknown'; }}

    protected getGenerator(String sourceType) {
        if (generators[sourceType] == null) {
            switch(sourceType) {
                default:
                    generators[sourceType] =
                        new LiterateMarkdownGenerator(this) }}

        return generators[sourceType] }

    protected getParser(String sourceType) {
        if (parsers[sourceType] == null) {
            switch(sourceType) {
                case 'erlang':
                    parsers[sourceType] = Parboiled.createParser(
                        JLPPegParser, '%%')
                    break
                case 'markdown':
                    parsers[sourceType] = new MarkdownParser()
                    break
                case 'c':
                case 'c++':
                case 'groovy':
                case 'java':
                case 'javascript':
                default:
                    parsers[sourceType] = Parboiled.createParser(JLPPegParser,
                        '/**', '*/', '!#$%^&*()_-=+|;:\'",<>?~`', '///')
                    break }}

        return parsers[sourceType] }
}
