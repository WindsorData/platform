package libt

/**
 * Type mirror of Element
 * @author flbulgarelli
 */
sealed trait TElement {
  
  /**
   * Answers the TValue element for a path that points to a TValue.
   * If the path points to something else, the result is undefined
   * */
  def apply(path: Path): TValue
  def validate(elemnt: Element) = ()
}

/**
 * Type mirror of Value
 * @author flbulgarelli
 * */
sealed trait TValue extends TElement {
  override def apply(path: Path) = path match {
    case Nil => this
  }
}
case object TString extends TValue
case object TInt extends TValue
case object TBool extends TValue
case object TDate extends TValue
case object TNumber extends TValue
case class TEnum(values: String*) extends TValue {
  private val valuesSet = values.toSet
  private def isValue = valuesSet.contains(_)
  override def validate(element: Element) = element match {
    case v: Value[String] => assert(v.value.forall(isValue(_)))
  }
}

/**
 * Type mirror of Col
 * @author flbulgarelli
 */
case class TCol(tElement: TElement) extends TElement {
  override def apply(path: Path) = path match {
    case Index(_) :: tail => tElement(tail)
  }
  override def validate(element: Element) = element match {
    case c: Col => c.elements.foreach(tElement.validate(_))
  }
}

/**
 * Type mirror of Model
 * @author flbulgarelli
 */
case class TModel(elements: (Symbol, TElement)*) extends TElement {
  private val elementsMap = elements.toMap

  override def apply(path: Path) = path match {
    case Route(field) :: tail => elementsMap(field)(tail)
  }

  override def validate(element: Element) = element match {
    case m: Model => elements.foreach {
      case (field, telement) => telement.validate(m(field))
    }
  }
}
