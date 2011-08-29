package com.jdblabs.jlp

public class TransparentFormatter implements Formatter {

    public String formatText(String text) { return text }
    public String formatCode(String code) { return code }
    public String formatReference(String ref) { return "ref#${ref}" } }
