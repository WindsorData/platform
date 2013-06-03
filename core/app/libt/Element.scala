package libt
import sys._
import util._
import output._

/**
 * @author flbulgarelli
 */
sealed trait Element extends ElementLike[Element] {
  override type ModelType = Model
  override type ColType = Col
  override type ValueType[A] = Value[A]

  /**
   * *
   * Answers the elements at given field key.
   * Fails when the key points to something else than a Col.
   *
   * This method only works for keyed elements - Models.
   */
  def c(key: Symbol) = this(Path(key)).asCol.elements
  
  def applySeq(path : Path) : Seq[Element] = umatch((path, this)) {
    case (* :: tail, self: Col) => self.elements.map(_.apply(tail))
    case (Route(field) :: tail, self: Model) => self(field).applySeq(tail)
  }
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

  /**Answers the seq of metadata elements of this value*/
  def metadataSeq = Seq(calc, comment, note, link)   
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
  def size = elements.size
}

/*=======Model=======*/

case class Model(elements: Set[(Symbol, Element)])
  extends Element
  with ModelLike[Element] {

  private val elementsMap = elements.toMap
  
  override def apply(key: Symbol) = elementsMap(key)
  
  def hasElement(key: Symbol) = elementsMap.contains(key)
  
  /**Creates a new model that is a submodel of this one that only
   * contains the elements of the given pk*/
  def subModel(pk: PK) = 
    Model(pk.map(path => (path.last.routeValue -> this(path))).toSet)

  /**
   * Flattens this model based on a path - flatteningPath - that points
   * to a Col of Models inside this model.
   *
   * It converts this model into a sequence
   * of new models, where each of them is built from the elements in the col of model,
   * plus the elements that conform the pk of this model - the rootPK.
   */
  def flattenWith(rootPk: PK, flatteningPath: Path): Seq[Model] =
    for (element <- this(flatteningPath).asCol.elements)
      yield element.asModel ++ this.subModel(rootPk)

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
  
  /**Converts a sequence of models into a sequence of flattened models*/
  def flattenWith(models: Seq[Model], rootPk: PK, flatteningPath: Path): Seq[Model] =
    models.flatMap(_.flattenWith(rootPk, flatteningPath))
}

