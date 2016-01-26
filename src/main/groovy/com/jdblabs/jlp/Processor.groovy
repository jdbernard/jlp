/**
 * # Processor
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright 2011-2012 [JDB Labs LLC](http://jdb-labs.com)
 */
package com.jdblabs.jlp

import com.jdblabs.jlp.LinkAnchor.LinkType
import com.jdbernard.util.JarUtils
import java.util.jar.JarInputStream
import org.parboiled.BaseParser
import org.parboiled.Parboiled
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

    /// The CSS that will be used for the resulting HTML documents. This object
    /// can be any object that responds to the `text` property.
    public def css

    /// A shortcut for `docs[currentDocId]`
    public TargetDoc currentDoc

    /// Setting to control whether the source code is copied into the final
    /// documentation directory or not.
    public boolean includeSource

    /// ### Non-public State
    /// @org jlp.jdb-labs.com/Processor/non-public-state

    /// Maps of all the parsers and generators by input file type. Parsers and
    /// generators are both safe for re-use within a single thread, so we cache
    /// them here.
    protected Map<String, JLPParser> parsers = [:]
    protected Map<String, JLPBaseGenerator> generators = [:]

    private Logger log = LoggerFactory.getLogger(getClass())

    /// ### Public Methods.
    /// @org jlp.jdb-labs.com/Processor/public-methods

    /**
     * #### Processor.process
     * @org jlp.jdb-labs.com/Processor/process
     * @api Process the input files given, writing the resulting documentation
     * to the directory named in `outputDir`, using the CSS given in `css`
     */
    public static void process(File outputDir, def css,
    List<File> inputFiles, boolean includeSource) {

        /// Find the closest common parent folder to all of the files given.
        /// This will be our input root for the parsing process.
        File inputDir = inputFiles.inject(inputFiles[0]) { commonRoot, file ->
            getCommonParent(commonRoot, file) }

        /// Create an instance of this class with the options given.
        Processor inst = new Processor(
            inputRoot: inputDir,
            outputRoot: outputDir,
            css: css,
            includeSource: includeSource)

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

        /// * Write the CSS file to our output directory.
        File cssFile = new File(outputRoot, ".css/jlp.css")
        cssFile.parentFile.mkdirs()
        cssFile.text = css.text

        /// * Extract the syntax highlighter files to the output directory.
        File shFile = new File(outputRoot, "temp.jar")
        File shDir = new File(outputRoot, "sh")
        File metaDir = new File(outputRoot, "META-INF")
        getClass().getResourceAsStream("/syntax-highlighter.jar").withStream { is ->
            shFile.withOutputStream { os ->
                while (is.available()) { os.write(is.read()) }}}
        JarUtils.extract(shFile, outputRoot)
        shDir.renameTo(new File(outputRoot, '.sh'))

        /// * Delete our temporary jar file and the META-INF directory extracted
        /// from it.
        shFile.delete()
        metaDir.deleteDir()

        /// * Create the processing context for each input file. We are using
        ///   the name of the file (including the extension) as the id. If there
        ///   is more than one file with the same name we will include the
        ///   file's parent directory as well.
        inputFiles.each { file ->

            // Get the relative path as path elements.
            def relPath = getRelativeFilepath(inputRoot, file)
            def pathParts = relPath.split('/|\\\\') as List

            // We will skip binary files and files we know nothing about.
            def fileType = sourceTypeForFile(file)
            if (fileType == 'binary' || fileType == 'unknown') { return; }

            // Start with just the file name.
            def docId = pathParts.pop()

            log.trace("New target document: '{}' from source: '{}'",
                docId, relPath)

            // As long as the current id is taken, add the next parent directory
            // to the id.
            while(docs[docId] != null) { docId = pathParts.pop() + '/' + docId }

            // Finally create the TargetDoc item.
            docs[docId] = new TargetDoc(
                sourceDocId: docId,
                sourceFile: file,
                sourceType: sourceTypeForFile(file)) }

        /// * Run the parse phase on each of the files. For each file, we load
        ///   the parser for that file type and parse the file into an abstract
        ///   syntax tree (AST).
        def badDocs = []
        processDocs {
            log.trace("Parsing '{}'.", currentDocId)
            def parser = getParser(currentDoc.sourceType)

            // TODO: better error detection and handling
            currentDoc.sourceAST = parser.parse(currentDoc.sourceFile.text)

            if (currentDoc.sourceAST == null) {
                log.warn("Unable to parse '{}'. Ignoring this document.", currentDocId)
                badDocs << currentDocId }}

        /// * Remove all the documents we could not parse from our doc list.
        docs = docs.findAll { docId, doc -> !badDocs.contains(docId) }

        /// * Run our generator parse phase (see
        ///   [`JLPBaseGenerator`](jlp://com.jdb-labs.jlp.JLPBaseGenerator/phases)
        ///   for an explanation of the generator phases).
        processDocs {
            log.trace("Second-pass parsing for '{}'.", currentDocId)
            def generator = getGenerator(currentDoc.sourceType)
            // TODO: error detection
            generator.parse(currentDoc.sourceAST) }


        /// * Second pass by the generators, the emit phase.
        processDocs {
            log.trace("Emitting documentation for '{}'.", currentDocId)
            def generator = getGenerator(currentDoc.sourceType)
            currentDoc.output = generator.emit(currentDoc.sourceAST) }

        /// * Write the output to the output directory.
        processDocs {

            /// Create the path and file object for the output file
            String relativePath =
                getRelativeFilepath(inputRoot, currentDoc.sourceFile)

            File outputFile = new File(outputRoot, relativePath + ".html")
            File outputDir = outputFile.parentFile

            log.trace("Saving output for '{}' to '{}'",
                currentDocId, outputFile)

            /// Create the directory for this file if it does not exist.
            if (!outputDir.exists()) { outputDir.mkdirs() }

            /// Copy the source file over.
            if (includeSource) {
                (new File(outputRoot, relativePath)).withWriter {
                    it.print currentDoc.sourceFile.text }}

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

    /***
     * #### resolveLink
     * Given a link, resolve it against the current output root.
     *
     * If this is a full URL, then we will attempt to resolve it based on the
     * URL protocol. If this is not a full URL then it will resolve the link
     * against the output root.
     *
     * `jlp://`
     * :   Resolve the link by looking for a matching link anchor defined
     *     in the documentation.
     *
     * *other protocol*
     * :   Return the link as-is.
     *
     * *absolute path (starts with `/`)*
     * :   Returns the link resolved against the output root.
     *
     * *relative path (no leading `/`)*
     * :   Returns the link resolved against the `TargetDoc` passed in.
     *
     * @org jlp.jdb-labs.com/Processor/resolveLink
     */
    public String resolveLink(String link, TargetDoc targetDoc) {
        switch (link) {

            /// JLP link, let's resolve with a link anchor
            case ~/^jlp:.*/:
                /// Get the org data we found in the parse phase for this org id.
                def m = (link =~ /jlp:\/\/(.+)/)
                def linkId = m[0][1]
                def linkAnchor = linkAnchors[m[0][1]]

                if (!linkAnchor) {
                    // We do not have any reference to this id.
                    log.warn("Unable to resolve a jlp link: {}.", link)
                    return "broken_link(${linkId})" }

                /// If this is a `FileLink` then we do not need the actual
                /// linkId, just the file being linked to.
                if (linkAnchor.type == LinkType.FileLink) { linkId = "" }

                /// This link points to a location in this document.
                if (targetDoc.sourceDocId == linkAnchor.sourceDocId) {
                    return "#${linkId}" }

                /// The link should point to a different document.
                else {
                    TargetDoc linkDoc = docs[linkAnchor.sourceDocId]

                    String pathToLinkedDoc = getRelativeFilepath(
                        targetDoc.sourceFile.parentFile, linkDoc.sourceFile)

                    return "${pathToLinkedDoc}.html#${linkId}" }

            /// Other protocol: return as-is.
            case ~/^\w+:.*/: return link

            /// Absolute link, resolve relative to the output root.
            case ~/^\/.*/:
                /// Our link should be the relative path (if needed) plus the
                /// link without the leading `/`.
                def relPath = getRelativeFilepath(targetDoc.sourceFile, inputRoot)
                return (relPath ? "${relPath}/" : "") + link[1..-1]

            /// Relative link, resolve using the output root and the source
            /// document relative to the input root.
            default:
                def relPath = getRelativeFilepath(inputRoot, targetDoc.sourceFile)
                return "${relPath}/${link}" }}

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
        def rootPath = root.canonicalPath.split('/|\\\\')
        def filePath = file.canonicalPath.split('/|\\\\')

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
            def path1 = file1.canonicalPath.split('/|\\\\')
            def path2 = file2.canonicalPath.split('/|\\\\')
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
            case 'c': case 'h': return 'c'
            case 'c++': case 'h++': case 'cpp': case 'hpp': return 'cpp'
            case 'erl': case 'hrl': return 'erlang'
            case 'groovy': return 'groovy'
            case 'java': return 'java'
            case 'js': return 'javascript'
            case 'md': return 'markdown'
            case 'html': return 'html'
            case 'xml': case 'xhtml': return 'xml'
            case 'prg': return 'foxpro'
            case 'sql': return 'sql'

            // binary file types
            case 'bin': case 'com': case 'exe': case 'o':
            case 'bz2': case 'tar': case 'tgz': case 'zip': case 'jar':
                return 'binary'
            default: return 'unknown'; }}

    /**
     * #### shBrushForSourceType
     * Lookup the syntax highlighter brush for the given source type.
     * @org jlp.jdb-labs.com/Processor/shBrushForSourceType
     */
    public static String shBrushForSourceType(String sourceType) {
        switch (sourceType) {
            case 'c': case 'cpp': return 'shBrushCpp'
            case 'erlang': return 'shBrushErlang'
            case 'groovy': return 'shBrushGroovy'
            case 'java': return 'shBrushJava'
            case 'javascript': return 'shBrushJScript'
            case 'html': case 'xml': return 'shBrushXml'
            case 'sql': return 'shBrushSql'
            default: return null }}

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
                case 'foxpro':
                    parsers[sourceType] = Parboiled.createParser(
                        JLPPegParser, ['**', '&&&'])
                    break
                case 'markdown':
                    parsers[sourceType] = new MarkdownParser()
                    break
                case 'html': case 'xml':
                    parsers[sourceType] = Parboiled.createParser(
                        JLPPegParser, '<!--!', '-->',
                        '!#$%^&*()_-+=|;:\'",<>?~`', '<<?')
                    break
                case 'sql':
                    parsers[sourceType] = Parboiled.createParser(JLPPegParser,
                        '/**', '*/', '!#$%^&*()_-=+|;:\'",<>?~`', '---')
                    break
                case 'c':
                case 'cpp':
                case 'groovy':
                case 'java':
                case 'javascript':
                default:
                    parsers[sourceType] = Parboiled.createParser(JLPPegParser,
                        '/**', '*/', '!#$%^&*()_-=+|;:\'",<>?~`', '///')
                    break }}

        return parsers[sourceType] }
}
