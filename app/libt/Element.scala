package libt
import sys.error
import util._
import output.PK

/**
 * @author flbulgarelli
 */
sealed trait Element extends ElementLike[Element] {
  override type ModelType = Model
  override type ColType = Col
  override type ValueType[_] = Value[_]

  /**
   * *
   * Answers the element at the given field key.
   *
   * This method only works for keyed elements - Models.
   */
  def m(key: Symbol): Element = this(Path(key))

  /**
   * *
   * Answers the Value at given field key.
   * Fails when the key points to something else than a Value.
   *
   * This method only works for keyed elements - Models.
   */
  def v[A](key: Symbol) = m(key).asValue[A]

  /**
   * *
   * Answers the elements at given field key.
   * Fails when the key points to something else than a Col.
   *
   * This method only works for keyed elements - Models.
   */
  def c(key: Symbol) = m(key).asCol.elements
}

/*=======Value=======*/

/**
 * A Value, with the actual basic value, plus metadata fields
 */
case class Value[A](
  value: Option[A],
  calc: Option[String],
  comment: Option[String],
  note: Option[String],
  link: Option[String]) 
  extends Element 
  with ValueLike[Element, A] {

  def contains(rawValue: A) =
    value.exists(_ == rawValue)

  /**Maps over the value*/
  def map[B](f: A => B) =
    Value(value.map(f), calc, comment, note, link)

  /**
   * Answers this Value if its basic value is defined, otherwise answers
   * a new Value with the basic value updated using the given alternative
   */
  def orDefault(alternative: => A) =
    if (value.isDefined)
      this
    else
      Value(Some(alternative), calc, comment, note, link)
}
object Value {
  
  def apply[A](): Value[A] = Value(None, None, None, None, None)
  
  def apply[A](value: A): Value[A] = Value(Some(value), None, None, None, None)
  
  def apply[A](
    value: Option[A],
    note: Option[String],
    link: Option[String]): Value[A] = Value(value, None, None, note, link)
}

/*=======Col=======*/

case class Col(elements: Element*)
  extends Element
  with ColLike[Element] {
  
  override def apply(index: Int) = elements(index)
  
}

/*=======Model=======*/

case class Model(elements: Set[(Symbol, Element)])
  extends Element
  with ModelLike[Element] {

  private val elementsMap = elements.toMap
  
  override def apply(key: Symbol) = elementsMap(key)

  def flattenWith(rootPk: PK, flatteningPath: Path): Seq[Model] =
    for (element <- this(flatteningPath).asCol.elements)
      yield element.asModel ++ Model(rootPk.map(path => (path.last.routeValue -> this(path))).toSet)

  /**
   * Model addition
   * 
   * Answers a new model that contains the elements of {{this}} and {{that}}. 
   * If there is collision between elements keys, the elements of {{that}} 
   * take precedence
   * */
  def ++(that: Model) = Model(elements ++ that.elements)
}
object Model {
  def apply(elements: (Symbol, Element)*): Model = Model(elements.toSet)
}

