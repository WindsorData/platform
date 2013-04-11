package libt
import sys.error

/**
 * @author flbulgarelli
 */
sealed trait Element {
  
  /***
   * Answers the element at the given field key.
   * 
   * This method only works for keyed elements - Models.  
   * */
  def m(key : Symbol) :  Element = error("unsupported operation")
  
  /***
   * Answers the Value at given field key.
   * Fails when the key points to something else than a Value.
   * 
   * This method only works for keyed elements - Models.
   * */
  def v[A](key : Symbol) = m(key).asInstanceOf[Value[A]]
  
  /***
   * Answers the elements at given field key.
   * Fails when the key points to something else than a Col.
   * 
   * This method only works for keyed elements - Models.
   * */
  def c(key : Symbol) = m(key).asInstanceOf[Col].elements
  def mc(key : Symbol)(pos : Int) = c(key)(pos).asInstanceOf[Model]
  def vc[A](key : Symbol)(pos : Int) = c(key)(pos).asInstanceOf[Value[A]]
}

/**
 * A Value, with the actual basic value, plus metadata fields
 */
case class Value[A](
  value: Option[A],
  calc: Option[String],
  comment: Option[String],
  note: Option[String],
  link: Option[String]) extends Element {
  def map[B](f: A => B) =
    Value(value.map(f), calc, comment, note, link)
}
object Value {
  def apply[A](): Value[A] = Value(None, None, None, None, None)
  def apply[A](value: A): Value[A] = Value(Some(value), None, None, None, None)
  def apply[A](
    value: Option[A],
    note: Option[String],
    link: Option[String]): Value[A] = Value(value, None, None, note, link)
}
case class Col(elements: Element*) extends Element

case class Model(elements: Set[(Symbol, Element)]) extends Element {
  private val elementsMap = elements.toMap
  def apply(key: Symbol) = elementsMap(key)
  override def m(key: Symbol) = this(key)
}
object Model {
  def apply(elements : (Symbol, Element)*) : Model = Model(elements.toSet)
}

