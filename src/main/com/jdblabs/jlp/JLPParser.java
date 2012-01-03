/**
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright JDB Labs 2010-2011
 */
package com.jdblabs.jlp;

import com.jdblabs.jlp.ast.SourceFile;

/**
 * JLPParser is a simple interface. It has one method to return a parsed
 * [`SourceFile`] given an input string. It may be expanded in the future to
 * be an abstract class implementing methods that take additional input for
 * convenience.
 *
 * [`SourceFile`]: jlp://jlp.jdb-labs.com/SourceFile
 *
 * @org jlp.jdb-labs.com/JLPParser
 */
public interface JLPParser {
    public SourceFile parse(String input); }
