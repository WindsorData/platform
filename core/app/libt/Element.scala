package libt

import libt.util._

trait ElementValueOps { self : Element =>
  def isEmpty(route:PathPart) = (this / route).rawValue.isEmpty
  def nonEmpty(route:PathPart) = (this / route).rawValue.nonEmpty

  def /#(route:PathPart) = (this / route).rawValue[Int]
  def /%(route:PathPart) = (this / route).rawValue[BigDecimal]
  def /!(route:PathPart) = (this / route).rawValue[String]

  def /#/(route:PathPart) = (this /# route).get
  def /%/(route:PathPart) = (this /% route).get
  def /!/(route:PathPart) = (this /! route).get

  def getRawValue[A] : A = rawValue[A].get
}

/**
  * @author flbulgarelli
 */
sealed trait Element extends ElementLike[Element] with ElementValueOps {
  override type ModelType = Model
  override type ColType = Col
  override type ValueType[A] = Value[A]

  def rawValue[A]  : Option[A] =  asValue[A].value

  /** Answers the elements at the given key,
    * spreading at the {{{*}}} path part if present.
    * */
  def applySeq(path : Path) : Seq[Element] = umatch((path, this)) {
    case (* :: tail, self: Col) => self.elements.map(_.apply(tail))
    case (* :: tail, self) => Seq(self.apply(tail))
    case (Route(field) :: Nil, self: Model) => Seq(self.get(field)).flatten
    case (Route(field) :: tail, self: Model) => self.get(field).toSeq.flatMap(_.applySeq(tail))
  }

  /**
   * Answers the elements at given pathPart, spreding at such pathPart.
   *
   *This method is a shortcut  for the more general [[libt.Element.applySeq]]  that takes a Path, when
   * it is a single [[libt.PathPart]] path followed by a {{{*}}}
   */
  def applySeq(pathPart: Symbol) : Seq[Element] = this.applySeq(Path(pathPart, *))
  
  /**
   * Answers true if all elements of this specific element has values.
   */
  def isComplete : Boolean
  
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
  
  def isComplete = value.nonEmpty
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
  def isComplete = elements.forall(_.isComplete)
}

/*=======Model=======*/

trait ModelAlgebraOps {  this : Model =>
  /**
   * Adds a complete model to this model
   *
   * Answers a new model that contains the elements of {{this}} and {{that}}.
   * If there is collision between elements keys, the elements of {{that}}
   * take precedence
   * */
  def ++(that: Model) = Model(elements ++ that.elements)

  /**Adds or updates a model entry to this model*/
  def +(entry:(Symbol, Element)) = Model(elements + entry)

  /** Removes a key from the given model */
  def -(key: Symbol): Model = Model(elements.filter{ case (k, _) => k != key })

  /**Creates a new model that is a submodel of this one that only
    * contains the elements of the given mask*/
  def intersect(mask: Seq[Path]) =
    Model(mask.map(path => (path.last.routeValue -> this(path))).toSet)
}

case class Model(elements: Set[(Symbol, Element)])
  extends Element
  with ModelLike[Element]
  with ModelLikeOps[Element]
  with ModelAlgebraOps {

  private val elementsMap = elements.toMap

  def get(key: Symbol) =  elementsMap.get(key)
  def contains(key: Symbol) = elementsMap.contains(key)

  /**
   * Flattens this model based on a path - flatteningPath - that points
   * to a Col of Models inside this model or to other element that can be converted
   * to a Col of a single Element.
   *
   * It converts this model into a sequence
   * of new models, where each of them is built from the elements in the col of model,
   * plus the elements that conform the pk of this model - the rootPK.
   */
  def flattenWith(rootPk: PK, flatteningPath: Path): Seq[Model] =
    for (element <- applySeq(flatteningPath))
     yield element.asModel ++ (this intersect rootPk)

  def isComplete = elements.forall { case (_, element) => element.isComplete }
}
object Model {
  def apply(elements: (Symbol, Element)*): Model = Model(elements.toSet)
  
  /**Converts a sequence of models into a sequence of flattened models*/
  def flattenWith(models: Seq[Model], rootPk: PK, flatteningPath: Path): Seq[Model] =
    models.flatMap(_.flattenWith(rootPk, flatteningPath))
}

