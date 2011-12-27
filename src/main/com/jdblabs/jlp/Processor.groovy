/**
 * @author Jonathan Bernard
 * @copyright JDB Labs 2010-2011
 */
package com.jdblabs.jlp

import org.parboiled.BaseParser
import org.parboiled.Parboiled

/**
 * Processor processes one batch of input files to create a set of output files.
 * It holds the intermediate state needed by the generators and coordinates the
 * work of the parsers and generators for each of the input files.
 * @org jlp.jdb-labs.com/Processor
 */
public class Processor {

    /// ### Public State
    /// @org jlp.jdb-labs.com/Processor/public-state

    /// A map of all the link anchors defined in the documents.
    public Map<String, LinkAnchor> linkAnchors = [:]

    /// A map of all the documents being processed.
    public Map<String, TargetDoc> docs = [:]

    /// The id of the document currently being processed.
    public String currentDocId = null

    /// The root of the input path.
    public File inputRoot

    /// The root of the output path.
    public File outputRoot

    /// The CSS that will be used for the resulting HTML documents. Note that
    /// this is the CSS file contents, not the name of a CSS file.
    public String css

    /// A shortcut for `docs[currentDocId]`
    public TargetDoc currentDoc

    /// ### Non-public State
    /// @org jlp.jdb-labs.com/Processor/non-public-state

    /// Maps of all the parsers and generators by input file type. Parsers and
    /// generators are both safe for re-use within a single thread, so we cache
    /// them here.
    protected Map<String, JLPParser> parsers = [:]
    protected Map<String, JLPBaseGenerator> generators = [:]

    /// ### Public Methods.
    /// @org jlp.jdb-labs.com/Processor/public-methods

    /**
     * #### Processor.process
     * @org jlp.jdb-labs.com/Processor/process
     * @api Process the input files given, writing the resulting documentation
     * to the directory named in `outputDir`, using the CSS given in `css`
     */
    public static void process(File outputDir, String css,
    List<File> inputFiles) {

        /// Find the closest common parent folder to all of the files given.
        /// This will be our input root for the parsing process.
        File inputDir = inputFiles.inject(inputFiles[0]) { commonRoot, file ->
            getCommonParent(commonRoot, file) }

        /// Create an instance of this class with the options given.
        Processor inst = new Processor(
            inputRoot: inputDir,
            outputRoot: outputDir,
            css: css)

        /// Run the process.
        inst.process(inputFiles) }

    /// ### Non-Public implementation methods.
    /// @org jlp.jdb-labs.com/Processor/non-public-methods

    /**
     * #### process
     * @org jlp.jdb-labs.com/Processor/process2
     */
    protected void process(inputFiles) {

        /// Remember that the data for the processing run was initialized by the
        /// constructor.

        /// * Create the processing context for each input file. We are using
        ///   the relative path of the file as the document id.
        inputFiles.each { file ->
            def docId = getRelativeFilepath(inputRoot, file)
            docs[docId] = new TargetDoc(sourceFile: file) }

        /// * Run the parse phase on each of the files. For each file, we load
        ///   the parser for that file type and parse the file into an abstract
        ///   syntax tree (AST).
        processDocs {
            def parser = getParser(sourceTypeForFile(currentDoc.sourceFile))
            // TODO: error detection
            currentDoc.sourceAST = parser.parse(currentDoc.sourceFile.text) }

        /// * Run our generator parse phase (see
        ///   jlp://com.jdb-labs.jlp.JLPBaseGenerator/phases for an explanation
        ///   of the generator phases).
        processDocs {
            def generator = getGenerator(sourceTypeForFile(currentDoc.sourceFile))
            // TODO: error detection
            generator.parse(currentDoc.sourceAST) }


        /// * Second pass by the generators, the emit phase.
        processDocs {
            def generator = getGenerator(sourceTypeForFile(currentDoc.sourceFile))
            currentDoc.output = generator.emit(currentDoc.sourceAST) }

        /// * Write the output to the output directory.
        processDocs {
            
            /// Create the path and file object for the output file
            String relativePath =
                getRelativeFilepath(inputRoot, currentDoc.sourceFile)

            File outputFile = new File(outputRoot, relativePath + ".html")
            File outputDir = outputFile.parentFile

            /// Create the directory for this file if it does not exist.
            if (!outputDir.exists()) { outputDir.mkdirs() }

            /// Write the CSS file if it does not exist.
            File cssFile = new File(outputDir, "jlp.css")
            if (!cssFile.exists()) { cssFile.withWriter{ it.println css } }

            /// Copy the source file over.
            // TODO: make this behavior customizable.
            (new File(outputRoot, relativePath)).withWriter {
                it.print currentDoc.sourceFile.text }

            /// Write the output to the file.
            outputFile.withWriter { it.println currentDoc.output } } }

    /**
     * #### processDocs
     * A helper method to walk over every document the processor is aware of,
     * setting up the `currentDocId` and `currentDoc` variables before calling
     * the given closure.
     * @org jlp.jdb-labs.com/Processor/processDocs
     */
    protected def processDocs(Closure c) {
        docs.each { docId, doc ->
            currentDocId = docId
            currentDoc = doc

            return c() } }

    /**
     * #### getRelativeFilepath
     * Assuming our current directory is `root`, get the relative path to 
     * `file`.
     * @org jlp.jdb-labs.com/Processor/getRelativeFilepath
     */
    public static String getRelativeFilepath(File root, File file) {
        /// Make sure our root is a directory
        if (!root.isDirectory()) root= root.parentFile

        /// Split both paths into their individual parts.
        def rootPath = root.canonicalPath.split('/')
        def filePath = file.canonicalPath.split('/')

        def relativePath = []

        /// Find the point of divergence in the two paths by walking down their
        /// parts until we find a pair that do not match.
        int i = 0
        while (i < Math.min(rootPath.length, filePath.length) &&
               rootPath[i] == filePath[i]) { i++ }

        /// Backtrack from our root to our newly-found common parent directory.
        (i..<rootPath.length).each { relativePath << ".." }

        /// Add the remainder of the path from our common parent directory to
        /// our file.
        (i..<filePath.length).each { j -> relativePath << filePath[j] }

        /// Reconstitute the parts into one string.
        return relativePath.join('/') }

    /**
     * #### getCommonParent
     * Find the common parent directory to the given files.
     * @org jlp.jdb-labs.com/Processor/getCommonParent
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

    /**
     * #### sourceTypeForFile
     * Lookup the source type for a given file. We do a lookup based on the file
     * extension for file types we recognize.
     * @org jlp.jdb-labs.com/Processor/sourceTypeForFile
     */
    public static sourceTypeForFile(File sourceFile) {

        /// First we need to find the file extension.
        String extension
        def nameParts = sourceFile.name.split(/\./)

        /// If there is no extension, then this is a binary file.
        if (nameParts.length == 1) { return 'binary' }
        else { extension = nameParts[-1] }

        /// Lookup the file type by extension
        switch (extension) {
            case 'c': case 'h': return 'c';
            case 'c++': case 'h++': case 'cpp': case 'hpp': return 'c++';
            case 'erl': case 'hrl': return 'erlang';
            case 'groovy': return 'groovy';
            case 'java': return 'java';
            case 'js': return 'javascript';
            case 'md': return 'markdown';
            default: return 'unknown'; }}

    /**
     * #### getGenerator
     * Get a generator for the given source file type.
     * @org jlp.jdb-labs.com/Processor/getGenerator
     */
    protected getGenerator(String sourceType) {
        /// We lazily create the generators.
        if (generators[sourceType] == null) {
            switch(sourceType) {
                /// So far, all languages are using the vanilla
                ///[`LiterateMarkdownGenerator`]
                ///(jlp://jlp.jdb-labs.com/LiterateMarkdownGenerator)
                default:
                    generators[sourceType] =
                        new LiterateMarkdownGenerator(this) }}

        return generators[sourceType] }

    /**
     * #### getParser
     * Get a parser for the given source file type.
     * @org jlp.jdb-labs.com/Processor/getParser
     */
    protected getParser(String sourceType) {
        /// We are lazily loading the parsers also.
        if (parsers[sourceType] == null) {
            /// We do have different parsers for different languages.
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
