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
sealed trait Validated[+Message, +A] {
  /**
   * Validated value extractor. Will fail when this is invalid
   */
  def get: A

  /**
   * Answers the seq of messages associated to this Validated value.
   * Valid values have no messages, that is, have an empty seq
   */
  def messages: Seq[Message]

  def isValid : Boolean = false
  def isDoubtful : Boolean = false
  def isInvalid = false
  /**Whether this is a ValidLike instance*/
  final def isValidOrDoubtful = isValid || isDoubtful


  /**Applies the given function to the value of the validad, if not invalid*/
  def map[B](f: A => B): Validated[Message, B]
  def flatMap[E >: Message, B](f: A => Validated[E, B]): Validated[E, B]
  /**
   * Combines this and other validated, by returning an invalid
   * when any of theses are invalid, or combining values with the given function, otherwise
   * */
  final def andAlso[E >: Message, B, C](other: Validated[E,B])(f:(A, B) => C) : Validated[E, C] =
    (this, other) match {
      case (i @ Invalid(messages @ _*), _) => i appendMessages other.messages
      case (_, i : Invalid[E]) => i prependMessages this.messages
      case (_, _) => for (v1 <- this; v2 <- other) yield f(v1, v2)
    }

  /**
   * Similar to andAlso, but discarding the the first value when both this and other are not invalid
   * */
  def andThen[E >: Message, B](other: Validated[E,B]) =
    andAlso(other) { (v1, v2) => v2  }

  /**appends a seq of messages to this validated*/
  def appendMessages[E >: Message](messages : Seq[E]) : Validated[E, A]

  implicit def validated2PrependOps[E, A](validated : Validated[E, A]) = new {
    /**prepends a seq el messages to this validated*/
    def prependMessages[M <: E](messages:Seq[M]) : Validated[E, A] = (messages, validated) match {
      case (Seq(), v @ Valid(_)) => v
      case (m, Valid(v)) => Doubtful(v, m :_*)
      case (m, Doubtful(v, m2@_*)) => Doubtful(v, m ++ m2:_*)
      case (m, Invalid(m2@_*)) => Invalid(m ++ m2:_*)
    }
  }

}

trait ValidLike[+Message, +A] extends Validated[Message, A] {
  val value : A
  override def get = value
}

case class Valid[+A](value: A) extends ValidLike[Nothing, A] {
  override def messages = Nil

  override def isValid = true

  override def map[B](f: A => B) = Valid(f(value))
  override def flatMap[E >: Nothing, B](f: A => Validated[E, B]) = f(value)

  override def appendMessages[E >: Nothing](messages : Seq[E]) =
    if(messages.isEmpty) this else Doubtful(value, messages:_*)
}

case class Doubtful[+Message, +A](value: A, override val messages: Message*)
  extends ValidLike[Message, A] {

  override def isDoubtful = true

  override def map[B](f: A => B) = Doubtful(f(value), messages:_*)
  override def flatMap[E >: Message, B](f: A => Validated[E, B]) = f(value) prependMessages this.messages

  override def appendMessages[E >: Message](messages : Seq[E]) = Doubtful(value, this.messages ++ messages:_*)
}

case class Invalid[Message](override val messages: Message*) extends Validated[Message, Nothing] {

  override def get = sys.error("Invald value " + messages.mkString(","))

  override def isInvalid = true

  override def map[B](f: Nothing => B) = this
  override def flatMap[E >: Message, B](f: Nothing => Validated[E, B]) = this

  override def appendMessages[E >: Message](messages : Seq[E]) = Invalid(this.messages ++ messages :_*)
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
      case (Nil, valids) => {
        if (valids.exists(_.isDoubtful))
          Doubtful(valids.map(_.get), valids.flatMap(_.messages): _*)
        else
          Valid(valids.map(_.get))
      }
      case (invalids, _) => Invalid(invalids.flatMap(_.messages): _*)
    }

  //TODO remove
  def flatJoin[E, A](elements: Seq[Validated[E, Seq[A]]]): Validated[E, Seq[A]] =
    concat(elements) match {
      case Valid(v) => Valid(v.flatten)
      case Doubtful(v,w) => Doubtful(v.flatten, w)
      case i: Invalid[A] => i
    }
    
  implicit def seq2SeqOfValidated[M, A](self:Seq[Validated[M, A]]) = new {
    def concat = Validated.concat(self)
  }
}
