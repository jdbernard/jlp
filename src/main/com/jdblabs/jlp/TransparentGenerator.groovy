package com.jdblabs.jlp

import com.jdblabs.jlp.ast.*
import com.jdblabs.jlp.ast.Directive.DirectiveType
import java.util.List
import java.util.Map

public class TransparentGenerator extends JLPBaseGenerator {

    protected TransparentGenerator() {}

    public static Map<String, String> generateDocuments(Map<String,
    List<ASTNode>> sources) {
        TransparentGenerator inst = new TransparentGenerator()
        return inst.generate(sources) }

    protected String emit(TextBlock textBlock) { textBlock.value }
    protected String emit(Directive directive) {
        switch (directive.type) {
            case DirectiveType.Author:  return "Author: ${directive.value}\n"
            case DirectiveType.Doc:     return directive.value
            case DirectiveType.Example: return "Example: ${directive.value}"
            case DirectiveType.Org:     return "" } }
}
