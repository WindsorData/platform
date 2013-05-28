package libt.error

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
sealed trait Validated[+A] {
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
  /**
   * Answers a Valid if the block can be completed without exceptions, or an Invalid with the exception 
   * error message if it fails.
   * 
   * This constructor is similar to that it {{{Try}}}
   **/
  def apply[A](action: => A): Validated[A] = try Valid(action) catch {
    case e: Exception => Invalid(e.getMessage)
  }
}
