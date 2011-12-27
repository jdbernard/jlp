# Overview
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

This project is in its infancy and some of the larger goals are still unmet:

* *Syntax highligting.*

    All code blocks will be highlighted according to the language they are
    written in.

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

# Project Architecture

## Control and Flow

* [JLPMain](jlp://jlp.jdb-labs.com/JLPMain)

    The entry point to the JLP executable. Parses the command line input and
    sets up the processor.

* [Processor](jlp://jlp.jdb-labs.com/Processor)

    The Processor processes one batch of input files to create a set of output files.
    It holds the intermediate state needed by the generators and coordinates the
    work of the parsers and generators for each of the input files.

## Parsing

* [JLPParser](jlp://jlp.jdb-labs.com/JLPParser)

    A very simple interface for parsing JLP input.

## Abstract Syntax Tree

* [SourceFile](jlp://jlp.jdb-labs.com/ast/SourceFile)

    The top-level AST element. This represents a source file.
