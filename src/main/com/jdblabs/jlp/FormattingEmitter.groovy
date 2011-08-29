package com.jdblabs.jlp

import com.jdblabs.jlp.ast.*
import com.jdblabs.jlp.ast.Directive.DirectiveType

import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class FormattingEmitter extends JLPBaseEmitter {

    Formatter formatter
    private Logger log = LoggerFactory.getLogger(this.getClass())

    public FormattingEmitter(Formatter f, def generationState) {
        super(generationState)
        this.formatter = f }

    protected String emit(TextBlock textBlock) {
        return formatter.format(textBlock) }

    protected String emit(Directive directive) {
        switch (directive.type) {
            case DirectiveType.Author:
            case DirectiveType.Doc:
            case DirectiveType.Example:
            case DirectiveType.Org:
                def orgValue = directive.value
                if generationState.orgs.contains(orgValue) {
                    log.warn("Duplicate @org id: '${orgValue}'.")
                    def orgMatcher = (orgValue =~ /(.*)-(\d+)/
                    if (orgMatcher.matches()) {
                        orgValue = "${m[0][1]}-${(m[0][2] as int) + 1}" }
                    else { orgValue += "-1" } }

                generationState.orgs << orgValue
                formatter.formatReference(orgValue)
                break } }

    private formatText(String s) {
        // fix links to internal targets
        s = s.eachMatch(/jlp:\/\/([^\s]+)/, s)

        // format with formatter
        return formatter.formatText(s)
    }
}
