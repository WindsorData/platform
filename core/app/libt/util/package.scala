package libt

package object util {
  /**
   * a pattern-matching operator equivalent to native match expressions,
   * but that suppress non-exhaustive matching warnings
   */
  def umatch[A, B](o: A)(f: PartialFunction[A, B]) = f(o)

  def key[K, V] = (_: (K, V))._1
  def value[K, V] = (_: (K, V))._2

  implicit def seq2seqWithPadding[A](self: Seq[A]) = new {
    def zipWithPadding[B](other: Seq[B]) = {
      val maxLength = other.size.max(self.size)
      def padWithNone[C](seq: Seq[C]) = seq.view.map(Some(_)).padTo(maxLength, None)
      padWithNone(self).zip(padWithNone(other))
    }
  }
}