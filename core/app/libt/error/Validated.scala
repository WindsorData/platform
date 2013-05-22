package libt.error

sealed trait Validated[+A] {
  /**
   * Validated value extractor. Will fail when this is
   * not Valid
   */
  def get: A
  
  /**Whether this is a Valid instance*/
  def isValid: Boolean
  def isInvalid = !isValid

  def toOption: Option[A]
  def toErrorSeq: Seq[String]
  
  def map[B](f: A => B): Validated[B]
}
case class Valid[+A](value: A) extends Validated[A] {
  override def isValid = true
  override def toOption = Some(value)
  override def get = value
  override def toErrorSeq = Nil
  override def map[B](f: A => B) = Valid(f(value))
}
case class Invalid(errors: String*) extends Validated[Nothing] {
  override def isValid = false
  override def toOption = None
  override def get = sys.error("Invald value " + errors.mkString(","))
  override def toErrorSeq = errors
  override def map[B](f: Nothing => B) = this
}
object Validated {
  def apply[A](action: => A): Validated[A] = try Valid(action) catch {
    case e: Exception => Invalid(e.getMessage)
  }
}

object `package` {
  implicit def traverable2ImpureMapOps[A](self: Seq[A]) = new {
    def impureMap[B](f: A => B) = self.map(f)
  }

  implicit def validatedSeq2ValidatedSeqOps[A](self: Seq[Validated[A]]) = new {
    def hasErrors = self.exists(_.isInvalid)
    def errors = self.flatMap(_.toErrorSeq)
    def join: Validated[Seq[A]] =
      self.partition(_.isInvalid) match {
        case (Nil, valids) => Valid(valids.flatMap(_.toOption))
        case (invalids, _) => Invalid(invalids.flatMap(_.toErrorSeq): _*)
      }
  }
}