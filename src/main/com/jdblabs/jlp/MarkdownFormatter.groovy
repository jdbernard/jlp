package com.jdblabs.jlp

public class MarkdownFormatter implements Formatter {

    private PegDownProcessor pegdown

    public MarkdownFormatter() {
        pegdown = new PegDownProcessor() }

    public String formatText(String s) { pegdown.markdownToHtml(s) }

    public String formatCode(String s) {
        pegdown.markdownToHtml(s.replaceAll(/(^|\n)/, /$1    /)) }

    public String formatReference(String s) { '<a name="${s}"/>' }
}
