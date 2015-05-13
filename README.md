# J Literate Programming

* [Source](https://git.jdb-labs.com/jdb-labs/jlp)  
* [Annotated Source and Documentation](https://doc.jdb-labs.com/jlp/current/)

## Overview
*Jonathan's Literate Programming* is my take on literate programming.
This project grew out of a desire for a documentation system that:

* generates all documentation from source-code comments,
* is capable of facilitating both JavaDoc-style API documentation as well as
  literate programming style of documentation,
* has pluggable formatting (default to Markdown),

It is inspired by Donald Knuth's concept of literate programming, as well as
the Docco system. I wanted something that provided the readability of Docco
but was more full-featured. To that end, JLP currently features:

* *Documentation alongside code, distinct from normal comments.*

    JLP uses a javadoc-like extra delimiter to seperate normal comments from
    JLP comments.

* *Support for multiple languages out of the box.*

    JLP allows you to define custom comment delimiters for any language that
    supports single-line or multi-line comments. It comes configured with
    default settings for several languages. Ultimately I hope to cover most
    of the common programming languages.

* *Syntax highligting.*

    All code blocks will be highlighted according to the language they are
    written in.

This project is in its infancy and some of the larger goals are still unmet:

* *Code awareness.*

    JLP will understand the code it is processing. This will require building
    a parser for each supported language. By doing so JLP will be able to
    generate javadoc-style API documentation intelligently, and allow the
    author to reference code features in a native way (think javadoc @link
    but more generic).

* *Documentation Directives*

    Generally I want documentation to conform to code, not code to
    documentation, but I think some processing directives to JLP (how to
    combine several files into one, or split one in to many for example)
    would be useful.

    In the same line of thought, it would be usefull to be able to switch
    the presentation layer of the documentation system depending on the type
    of file being displayed. For example, interface definitions and core
    pieces of the API may work better with a side-by-side layout whereas
    implementation details may work better in an interleaved layout.
    JLP processing directives would allow the author to specify which is
    intended on a file (or block?) level.

## Project Architecture

JLP processes it's own documentation. The latest documentation is available at
https://doc.jdb-labs.com/jlp/current/

Below are some starting points.

### Control and Flow

* [JLPMain](https://doc.jdb-labs.com/jlp/current/src/main/groovy/com/jdblabs/jlp/JLPMain.groovy.html)

    The entry point to the JLP executable. Parses the command line input and
    sets up the processor.

* [Processor](https://doc.jdb-labs.com/jlp/current/src/main/groovy/com/jdblabs/jlp/Processor.groovy.html)

    The Processor processes one batch of input files to create a set of output files.
    It holds the intermediate state needed by the generators and coordinates the
    work of the parsers and generators for each of the input files. This
    processor only generates HTML documentation and will likely be renamed in
    the future to reflect this.

* [JLPBaseGenerator](https://doc.jdb-labs.com/jlp/current/src/main/groovy/com/jdblabs/jlp/JLPBaseGenerator.groovy.html)

    The Generator processes one input file. It parses the AST for the input file
    and emits the final documentation for the file. JLPBaseGenerator
    implementations are expected to be tightly coupled to Processor
    implementations.

* [LiterateMarkdownGenerator](https://doc.jdb-labs.com/jlp/current/src/main/groovy/com/jdblabs/jlp/LiterateMarkdownGenerator.groovy.html)

    This implemetation of JLPBaseGenerator generates literate-style
    documentation (as opposed to API-style), using
    [Markdown](http://daringfireball.net/projects/markdown/) to format the
    documentation blocks.

### Parsing

* [JLPParser](https://doc.jdb-labs.com/jlp/current/src/main/groovy/com/jdblabs/jlp/JLPParser.groovy.html)

    A very simple interface for parsing JLP input.

* [JLPPegParser](https://doc.jdb-labs.com/jlp/current/src/main/groovy/com/jdblabs/jlp/JLPPegParser.groovy.html)

    A [PEG parser](http://en.wikipedia.org/wiki/Parsing_expression_grammar)
    implemented using the [parboiled](http://www.parboiled.org) library. This
    is the default source code parser. It is able to parse JLP documentation
    but leaves code unparsed. It can be parameterized to fit the differing
    documentation styles of source languages.

### Abstract Syntax Tree

* [SourceFile](https://doc.jdb-labs.com/jlp/current/src/main/groovy/com/jdblabs/jlp/JLPPegParserSourceFile.groovy.html)

    The top-level AST element. This represents a source file.
