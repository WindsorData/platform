package libt.error.generic

/**
 * A validated functor, that either holds a value, or a list of error messages
 * that explain why the value could not be generated.
 * 
 * This functor is more specific than {{{Either}}} 
 * but more general than {{{Try}}}.
 * 
 * It is aimed to enable non-fail fast error handling
 * 
 * @author flbulgarelli
 */
sealed trait Validated[+ErrorMessage, +A] {
  /**
   * Validated value extractor. Will fail when this is
   * not Valid
   */
  def get: A
  
  /**Whether this is a Valid instance*/
  def isValid: Boolean
  def isInvalid = !isValid

  /**
   * Converts to an option: Valid values are converted to Some,
   * where Invalid are converted to None
   */
  def toOption: Option[A]
  /**
   * Converts to a seq of error messages.
   * Valid is converted to the empty list, while
   * Invalid is converted to a non-empty list
   */
  def toErrorSeq: Seq[ErrorMessage]

  /**Applies the given function to the value of the validad, if not invalid*/
  def map[B](f: A => B): Validated[ErrorMessage, B]
  def flatMap[E >: ErrorMessage, B](f: A => Validated[E, B]): Validated[E, B]
  /**
   * Combines this and other validated, by returning an invalid
   * when any of theses are invalid, or combining values with the given function, otherwise
   * */
  def andAlso[E>:ErrorMessage, B, C](other: Validated[E,B])(f:(A, B) => C) : Validated[E, C]
  /**
   * Similar to andAlso, but discarding the the first value when both this and other are not invalid
   * */
  def andThen[E>:Nothing, B](other: Validated[E,B]) =
    andAlso(other) { (v1, v2) => v2  }
}


case class Valid[+A](value: A) extends Validated[Nothing, A] {
  override def isValid = true
  override def toOption = Some(value)
  override def get = value
  override def toErrorSeq = Nil
  override def map[B](f: A => B) = Valid(f(value))
  override def flatMap[E >: Nothing, B](f: A => Validated[E, B]) = f(value)
  override def andAlso[E>:Nothing, B, C](other: Validated[E,B])(f:(A, B) => C) =
    other match {
      case Valid(value) => Valid(f(this.value, value))
      case i : Invalid[E] => i
    }
}
case class Invalid[ErrorMessage](errors: ErrorMessage*) extends Validated[ErrorMessage, Nothing] {
  override def isValid = false
  override def toOption = None
  override def get = sys.error("Invald value " + errors.mkString(","))
  override def toErrorSeq = errors
  override def map[B](f: Nothing => B) = this
  override def flatMap[E >: ErrorMessage, B](f: Nothing => Validated[E, B]) = this
  override def andAlso[E>:ErrorMessage, B, C](other: Validated[E,B])(f:(Nothing, B) => C) : Validated[E, C] =
    other match {
      case i : Invalid[E] => addErrors(i.errors)
      case _ => this
    }
  def addErrors[E >: ErrorMessage](errors: Seq[E]) = Invalid(this.errors ++ errors: _*)

}
object Validated {
  /**
   * Answers a Valid if the block can be completed without exceptions, or an Invalid with the exception 
   * error message if it fails.
   * 
   * This constructor is similar to that it {{{Try}}}
   **/
  def apply[A](action: => A): Validated[String, A] = try Valid(action) catch {
    case e: Exception => Invalid(e.getMessage)
  }

  /**
   * Merges this seq of validated values into
   * a single validated value, by answering a
   * Valid seq of the valid values, if all the values are valid, or an Invalid
   * with all the errors, otherwise
   */
  def concat[E, A](elements: Seq[Validated[E, A]]): Validated[E, Seq[A]] =
    elements.partition(_.isInvalid) match {
      case (Nil, valids) => Valid(valids.map(_.get))
      case (invalids, _) => Invalid(invalids.flatMap(_.toErrorSeq): _*)
    }

  //TODO remove
  def flatJoin[E, A](elements: Seq[Validated[E, Seq[A]]]): Validated[E, Seq[A]] =
    concat(elements) match {
      case Valid(v) => Valid(v.flatten)
      case i: Invalid[A] => i
    }
    
  implicit def seq2SeqOfValidated[M, A](self:Seq[Validated[M, A]]) = new {
    def concat = Validated.concat(self)
  }

}
