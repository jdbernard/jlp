SourceFile -> (DocBlock / CodeBlock)*

DocBlock -> (DirectiveBlock / MarkdownBlock)+

Code Block -> ((!DOC_START RemainingLine) / EmptyLine)+

DirectiveBlock -> DOC_START DIRECTIVE_START (LongDirective / LineDirective)

MarkdownBlock -> MarkdownLine+

LongDirective ->
    (AUTHOR_DIR / DOC_DIR / EXAMPLE_DIR) RemainingLine MarkdownBlock?

LineDirective -> ORG_DIR RemainingLine

MarkdownLine -> DOC_START !DIRECTIVE_START RemainingLine

RemainingLine -> (!EOL)+ (EOL / EOI)

EmptyLine -> EOL

Tokens
------

DOC_START           -> "%% "
EOL                 -> "\n"
DIRECTIVE_START     -> "@"


