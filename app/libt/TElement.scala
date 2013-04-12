package libt
import libt.util._
import java.util.Date

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
sealed trait TValue[A] extends TElement {
  override def apply(path: Path) =  umatch(path) {
    case Nil => this
  }
}

case class TWithDefault[A](tValue: TValue[A], defaultValue: A) extends TValue[A] {
  override def validate(element: Element) = umatch(element) {
    case v: Value[A] => tValue.validate(v)
  }
}
case object TString extends TValue[String]
case object TInt extends TValue[Int]
case object TBool extends TValue[Boolean]
case object TDate extends TValue[Date]
case object TNumber extends TValue[BigDecimal]
case class TEnum(values: String*) extends TValue[String] {
  private val valuesSet = values.toSet
  private def isValue = valuesSet.contains(_)
  override def validate(element: Element) = umatch(element) {
    case v: Value[String] => assert(v.value.forall(isValue(_)))
  }
}

/**
 * Type mirror of Col
 * @author flbulgarelli
 */
case class TCol(tElement: TElement) extends TElement {
  override def apply(path: Path) = umatch(path) {
    case Index(_) :: tail => tElement(tail)
  }
  override def validate(element: Element) = umatch(element) {
    case c: Col => c.elements.foreach(tElement.validate(_))
  }
}

/**
 * Type mirror of Model
 * @author flbulgarelli
 */
case class TModel(elements: (Symbol, TElement)*) extends TElement {
  private val elementsMap = elements.toMap

  override def apply(path: Path) = umatch(path ) {
    case Route(field) :: tail => elementsMap(field)(tail)
  }

  override def validate(element: Element) = umatch(element) {
    case m: Model => elements.foreach {
      case (field, telement) => telement.validate(m(field))
    }
  }
}
