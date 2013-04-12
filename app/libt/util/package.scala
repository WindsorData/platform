package libt

package object util {
  def umatch[A, B](o: A)(f: PartialFunction[A, B]) = f(o)
}