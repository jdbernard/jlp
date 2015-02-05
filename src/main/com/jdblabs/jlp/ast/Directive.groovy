/**
 * # Directive
 * @author Jonathan Bernard (jdb@jdb-labs.com)
 * @copyright 2011-2012 [JDB Labs LLC](http://jdb-labs.com)
 */
package com.jdblabs.jlp.ast

/**
 * @api ASTNode for `Directives`.
 * A documentation directive allows the author to add metadata and processing
 * instructions.
 * @org jlp.jdb-labs.com/ast/Directive
 */
public class Directive extends ASTNode {

    /**
     * There are currently five types of directive currently available:
     *
     * API
     * :   The *API* directive allows the author to seperate the parts of the
     *     documentation that should be included in javadoc-style API
     *     documentation, as opposed to full literate code-style documentation.
     *
     * Author
     * :   The *Author* directive is used to specify the author of a set of
     *     documentation. It can be used to denote the author of an entire file
     *     when used in the first documentation block or just the author of a
     *     specific method when used in the documentation block before that
     *     method.
     *
     * Copyright
     * :   Similar to *Author*, this directive allows you to mark the copyright
     *     information for a set of documentation.
     *
     * Example
     * :   Used to mark an example in the documentation. The full doc block
     *     following the directive will be included inline as an example,
     *     typically typeset in a monospace font.
     *
     * Include
     * :   Include the contents of a url inline in the documentation where this
     *     directive occurs. This is primarily intended to allow the author to
     *     include other parts of the documentation inline via the `jlp`
     *     protocol and link anchors, but it is not restriced to the `jlp`
     *     protocol. The include directive is followed by a URL to the resource
     *     to include. This can be any valid URL.
     * Org
     * :   Used to create a link anchor in the documentation. The `jlp` protocol
     *     in a URL allows the author to link back to a link anchor. Refer to
     *     the [`LinkAnchor`] documentation for more information about link
     *     anchors.
     *
     * Param
     * :   Used to document a parameter to a function, method, or subroutine.
     *     This is typically used in API documentation, but no generator
     *     currently knows how to do anything intelligent with this directive.
     *
     * [`LinkAnchor`]: jlp://jlp.jdb-labs.com/LinkAnchor
     */
    public static enum DirectiveType {
        Api, Author, Copyright, Example, Include, Org, Param;
        
        public static DirectiveType parse(String typeString) {
            valueOf(typeString.toLowerCase().capitalize()) } }

    public final DocBlock parentBlock;
    public final DirectiveType type;
    public final String value;

    public Directive(String value, String typeString, int lineNumber,
        DocBlock parentBlock) {
        super(lineNumber)
        this.value = value
        this.type = DirectiveType.parse(typeString)
        this.parentBlock = parentBlock }
        
    public String toString() { "[${lineNumber}:Directive: ${type}, ${value}]" }
}
