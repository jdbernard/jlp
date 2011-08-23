DocBlock -> Directive | MarkdownBlock

Directive ->
    "@author" MarkdownBlock |
    "@doc" MarkdownBlock |
    "@example" MarkdownBlock |
    "@org" LiteralString

MarkdownBlock ->
