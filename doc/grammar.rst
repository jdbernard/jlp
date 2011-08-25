CodePage -> (CodeBlock | DocBlock)*

// lookahead 2 needed here
DocBlock -> (DirectiveBlock | MarkdownBlock)+

DirectiveBlock ->
    <DOC_START> <DIRECTIVE_START> "author" RemainingLine EOL MarkdownBlock? |
    <DOC_START> <DIRECTIVE_START> "doc" RemainingLine EOL MarkdownBlock? |
    <DOC_START> <DIRECTIVE_START> "example" RemainingLine EOL MarkdownBlock? |
    <DOC_START> <DIRECTIVE_START> "org" OrgString EOL

MarkdownBlock -> MarkdownLine+

MarkdownLine ->
    <DOC_START> NOT_DIRECTIVE_START RemainingLine <EOL>

RemainingLine -> NOT_EOL*

OrgString ->
    (<ORG_ID> <SLASH>)* <ORG_ID> <SLASH>?

Tokens
------

DOC_START           -> "%% "
EOL                 -> "\n"
NOT_EOL             -> ~"\n"
DIRECTIVE_START     -> "@"
NOT_DIRECTIVE_START -> ~"@"
SLASH               -> "/"
ORG_ID              -> ~"[/\n]"
