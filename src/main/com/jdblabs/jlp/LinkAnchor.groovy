/**
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright JDB Labs 2010-2011
 */
package com.jdblabs.jlp

import com.jdblabs.jlp.ast.Directive

/**
 * A *LinkAnchor* in the documentation is very similar to an HTML anchor. It
 * creates a reference in the documentation that can used by the author to
 * link to this point.
 *
 * The author uses a URL with the `jlp` protocol to refer to anchors. For
 * example: `jlp://jlp.jdb-labs.com/LinkAnchor` refers to this documentation.
 *
 * @api LinkAnchor is a data class to hold information about the link anchors
 * defined by `@org` directives.
 * @org jlp.jdb-labs.com/LinkAnchor
 */
public class LinkAnchor {

    /// The anchor id. This comes from the text after the directive.
    public String id

    public Directive directive
    public String sourceDocId

}
