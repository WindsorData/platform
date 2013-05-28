package libt

package object error {
  
  //TODO this should be in another place
  implicit def traverable2ImpureMapOps[A](self: Seq[A]) = new {
    def impureMap[B](f: A => B) = self.map(f)
  }

  implicit def validatedSeq2ValidatedSeqOps[A](self: Seq[Validated[A]]) = new {
    /**Whether any of validated values is invalid*/
    def hasErrors = self.exists(_.isInvalid)
    /**All the - flatten - errors of all the validated values*/
    def errors = self.flatMap(_.toErrorSeq)
    /**
     * Merges this seq of validated values into
     * a single validated value, by answering a
     * Valid seq of the valid values, if all the values are valid, or an Invalid
     * with all the errors, otherwise
     */
    def join: Validated[Seq[A]] =
      self.partition(_.isInvalid) match {
        case (Nil, valids) => Valid(valids.flatMap(_.toOption))
        case (invalids, _) => Invalid(invalids.flatMap(_.toErrorSeq): _*)
      }
  }
}