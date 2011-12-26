package test;

import java.util.Array;

/** This is a test class. Mainly for testing my parser.
  * It should work. And now we are just filing space.
  * @author Jonathan Bernard
  * @copyright JDB Labs 2011 */
public class Test {

    /**
      | @org test-ref
      | This is an embedded comment.
      | It spreads over at least 3 lines.
      | And this is the third line.
      */

    public static void main(String[] args) {
        /** Yes, this is a hello world example. */
        System.out.println("Hello World!");
    }

    /// This is a single-line comment block. */ /** Other comment
    /// modifiers should not matter within this block.
    /// @org last-doc
}
