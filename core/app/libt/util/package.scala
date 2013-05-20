package libt

package object util {
  /**a pattern-matching operator equivalent to native match expressions, 
   * but that suppress non-exhaustive matching warnings*/
  def umatch[A, B](o: A)(f: PartialFunction[A, B]) = f(o)
}