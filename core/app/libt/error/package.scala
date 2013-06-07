package libt

package object error {
  
  type Validated[A] = generic.Validated[String, A]
  type Invalid = generic.Invalid[String]
  val Valid = generic.Valid
  val Validated = generic.Validated
  val Invalid = generic.Invalid
  
  //TODO this should be in another place
  implicit def traverable2ImpureMapOps[A](self: Seq[A]) = new {
    def impureMap[B](f: A => B) = self.map(f)
  }

  //TODO delete
  implicit def validatedSeq2ValidatedSeqOps[A](self: Seq[Validated[A]]) = new {
    /**Whether any of validated values is invalid*/
    def hasErrors = self.exists(_.isInvalid)
  }
  
  
  
}