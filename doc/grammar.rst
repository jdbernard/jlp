SourceFile ->
    (Block / DocBlock / CodeBlock)+

Block ->
    DocBlock CodeBlock

DocBlock ->
    (Directive / DocText)+

Directive ->
    DocLineStart AT (LongDirective / ShortDirective)

LongDirective ->
    ("author" / "doc" / "example") RemainingLine DocText?

ShortDirective ->
    ("org" / "copyright") RemainingLine

DocText ->
    (DocLineStart !AT RemainingLine)+

DocLineStart ->
    Space* DOC_LINE_START Space?

CodeBlock ->
    (!DocLineStart RemainingLine)+

RemainingLine ->
   ((!EOL)* EOL) / ((!EOL)+ EOI)
