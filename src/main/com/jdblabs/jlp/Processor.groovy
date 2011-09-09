package com.jdblabs.jlp

import org.parboiled.BaseParser
import org.parboiled.Parboiled
import org.parboiled.parserunners.ReportingParseRunner

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

    protected Map<Class, BaseParser> parsers = [:]
    protected Map<Class, JLPBaseGenerator> generators = [:]

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
            def parser = getParser(JLPPegParser)
            def parseRunner = new ReportingParseRunner(parser.SourceFile())

            currentDoc.sourceAST = parseRunner.run(
                currentDoc.sourceFile.text).resultValue }

        // generate output
        processDocs {

            // TODO: add logic to configure or autodetect the correct generator
            // for each file
            def generator = getGenerator(LiterateMarkdownGenerator)
            currentDoc.output = generator.generate(currentDoc.sourceAST) }

        // Write the output to the output directory
        processDocs {
            
            // create the path to the output file
            String relativePath =
                getRelativeFilepath(inputRoot, currentDoc.sourceFile)

            File outputFile = new File(outputRoot, relativePath + ".html")
            File outputDir = outputFile.parentFile

            // create the directory if need be
            if (!outputDir.exists()) {
                outputDir.mkdirs() }

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

    protected getGenerator(Class generatorClass) {
        if (generators[generatorClass] == null) {
            def constructor = generatorClass.getConstructor(Processor)
            generators[generatorClass] = constructor.newInstance(this)
        }

        return generators[generatorClass] }

    protected getParser(Class parserClass) {
        if (parsers[parserClass] == null) {
            parsers[parserClass] = Parboiled.createParser(parserClass) }

        return parsers[parserClass] }
}
