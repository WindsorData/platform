package libt

package object error {
  
  type Validated[A] = generic.Validated[String, A]
  val Valid = generic.Valid
  val Validated = generic.Validated
  val Invalid = generic.Invalid
  
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
        case (Nil, valids) => Valid(valids.map(_.get))
        case (invalids, _) => Invalid(invalids.flatMap(_.toErrorSeq): _*)
      }
  }
}