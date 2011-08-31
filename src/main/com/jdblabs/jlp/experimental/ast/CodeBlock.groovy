package com.jdblabs.jlp.experimental.ast

import java.util.Map

public class CodeBlock extends ASTNode {

    public Map<Integer, String> lines = [:]

    public CodeBlock(int lineNumber) { super(lineNumber) }

    public String toString() {
        def linesVal = ""
        lines.each { lineNum, value -> linesVal += "${lineNum}:${value}" }
            
        return "[${lineNumber}:CodeBlock: ${linesVal}]" }
}
