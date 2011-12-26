SourceFile ->
    (Block / DocBlock / CodeBlock)+

Block ->
    DocBlock CodeBlock

DocBlock ->
    (SDocBlock / MDocBlock)

SDocBlock ->
    (SDirective / SDocText)+

MDocBlock ->
    MDOC_START (!MDOC_END / MDirective / MDocText)* MDOC_END

CodeBlock ->
    (RemainingCodeLine)+

SDirective ->
    SDocLineStart AT (SLongDirective / SShortDirective)

MDirective ->
    MDocLineStart? AT (MLongDirective / MShortDirective)

SLongDirective ->
    ("api" / "example") RemainingSDocLine SDocText?

MLongDirective ->
    ("api" / "example") RemainingMDocLine MDocText?

SShortDirective ->
    ("author" / "org" / "copyright") RemainingSDocLine

MShortDirective ->
    ("author" / "org" / "copyright") RemainingMDocLine

SDocText ->
    (SDocLineStart !AT RemainingSDocLine)+

MDocText ->
    (MDocLineStart? !AT RemainingMDocLine)+

SDocLineStart ->
    SPACE* SDOC_START SPACE?

MDocLineStart ->
    SPACE* !MDOC_END MDOC_LINE_START SPACE?

RemainingSDocLine ->
    ((!EOL)* EOL) / ((!EOL)+ EOI)

RemainingMDocLine ->
    ((!(EOL / MDOC_END))* EOL) / ((!MDOC_END)+)

RemainingCodeLine ->
    ((!(EOL / MDOC_START / SDocLineStart))* EOL) /
    (!(MDOC_START / SDocLineStart))+
