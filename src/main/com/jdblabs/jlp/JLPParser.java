package com.jdblabs.jlp;

import com.jdblabs.jlp.ast.SourceFile;

public interface JLPParser {
    public SourceFile parse(String input); }
