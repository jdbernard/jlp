package com.jdblabs.jlp

import com.jdblabs.jlp.ast.*
import com.jdblabs.jlp.ast.Directive.DirectiveType
import java.util.List
import java.util.Map

public abstract class JLPBaseGenerator {

    protected Map docState

    protected JLPBaseGenerator() {
        docState = [orgs:           [:],
                    currentDocId:   false ] }

    protected Map<String, String> generate(Map<String, List<ASTNode>> sources) {
        Map result = [:]
        sources.each { sourceId, sourceNodes ->
            docState.currentDocId = sourceId
            result[sourceId] = emitDocument(sourceNodes) }
        return result }

    protected String emitDocument(List<ASTNode> sourceNodes) {
        StringBuilder result =
            sourceNodes.inject(new StringBuilder()) { sb, node ->
                sb.append(emit(node))
                return sb }

        return result.toString() }

    protected String emit(DocBlock docBlock) {
        List printQueue 
        StringBuilder result

        printQueue = docBlock.directives.collect { directive ->
            def queueItem = [line: directive.lineNumber, value: directive]
            switch (directive.type) {
                case DirectiveType.Author:  queueItem.priority = 50; break
                case DirectiveType.Doc:     queueItem.priority = 50; break
                case DirectiveType.Example: queueItem.priority = 50; break
                case DirectiveType.Org:     queueItem.priority = 10; break }

            return queueItem }

        printQueue.addAll(docBlock.textBlocks.collect { textBlock ->
            [ priority: 50, line: textBlock.lineNumber, value: textBlock ] })

        // sort by priority, then by line number
        printQueue.sort(
            {i1, i2 -> i1.priority != i2.priority ?
                        i1.priority - i2.priority :
                        i1.line - i2.line} as Comparator)
        

        result = printQueue.inject(new StringBuilder()) { sb, printable ->
            sb.append(emit(printable.value))
            return sb }

        return result.toString() } 

    protected abstract String emit(TextBlock textBlock)
    protected abstract String emit(Directive directive)
}
