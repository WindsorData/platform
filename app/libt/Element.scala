package libt
import sys.error
import util._
import output.PK

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
  def c(key : Symbol) = m(key).asCol.elements
  def mc(key : Symbol)(pos : Int) = c(key)(pos).asInstanceOf[Model]
  def vc[A](key : Symbol)(pos : Int) = c(key)(pos).asValue[A]
  
  def apply(path : Path) : Element = umatch((path, this)) {
    case (Nil, _) => this
    case (Index(i) :: tail, self : Col) => self(i)(tail)
    case (Route(field) :: tail, self : Model) => self(field)(tail)
  }
  
  //TODO Philosoraptor asks: should we use pattern matching or cast ?
  def asValue[A] = asInstanceOf[Value[A]]
  def asCol = asInstanceOf[Col]
  def asModel = asInstanceOf[Model]
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
  
  def contains(rawValue : A) = 
    value.exists(_ == rawValue)
  
  /**Maps over the value*/
  def map[B](f: A => B) =
    Value(value.map(f), calc, comment, note, link)

  /**Answers this Value if its basic value is defined, otherwise answers
   * a new Value with the basic value updated using the given alternative*/
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
case class Col(elements: Element*) extends Element {
  def apply(index: Int) = elements(index)
}

case class Model(elements: Set[(Symbol, Element)]) extends Element {
  private val elementsMap = elements.toMap
  def apply(key: Symbol) = elementsMap(key)
  override def m(key: Symbol) = this(key)
  
  def flattenWith(rootPk: PK, flatteningPath: Path): Seq[Model] =
    for (element <- this(flatteningPath).asCol.elements)
      yield element.asModel ++ Model(rootPk.elements.map(path => (path.last.routeValue -> this(path))).toSet)
  
  def ++(other: Model) = Model(elements ++ other.elements)
}
object Model {
  def apply(elements : (Symbol, Element)*) : Model = Model(elements.toSet)
}

