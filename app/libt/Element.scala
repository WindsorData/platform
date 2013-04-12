package libt

sealed trait Element

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

case class Model(elements: (Symbol, Element)*) extends Element {
  private val elementsMap = elements.toMap
  def apply(key: Symbol) = elementsMap(key)
}


