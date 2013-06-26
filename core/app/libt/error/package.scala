package libt

package object error {
  
  type Validated[A] = generic.Validated[String, A]
  type Invalid = generic.Invalid[String]
  type Doubtful[+A] = generic.Doubtful[String, A]
  
  val Valid = generic.Valid
  val Doubtful = generic.Doubtful
  val Validated = generic.Validated
  val Invalid = generic.Invalid
  
  //TODO this should be in another place
  implicit def traverable2ImpureMapOps[A](self: Seq[A]) = new {
    def impureMap[B](f: A => B) = self.map(f)
  }
}
