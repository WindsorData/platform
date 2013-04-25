package libt
import libt.util._
import java.util.Date

/**
 * Type mirror of Element. It is 
 * the base trait for defining a data schema
 * 
 * @author flbulgarelli
 */
sealed trait TElement {
  
  /**
   * Answers the TValue element for a path that points to a TValue.
   * Fails if the path points to something else
   * */
  def apply(path : Path) : TElement = umatch((path, this)) {
    case (Nil, _) => this
    case (Index(_) :: tail, self : TCol) => self.tElement(tail)
    case (Route(field) :: tail, self : TModel) => self(field)(tail)
  }
  
  /**
   * Validates the given element using this TElement as schema,
   * by failing if it does not conform to this TElement  
   * */
  def validate(element: Element) = ()
  
  def asValue[A] = asInstanceOf[TValue[A]]
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

/***
 * TValue wrapper for introducing default values information 
 * into the schema
 * 
 * @author flbulgarelli
 */
case class TWithDefault[A](tValue: TValue[A], defaultValue: A) extends TValue[A] {
  override def validate(element: Element) = umatch(element) {
    case v: Value[A] => tValue.validate(v)
  }
}
case object TString extends TValue[String]
case object TInt extends TValue[Int]
case object TBool extends TValue[Boolean]
case object TXBool extends TValue[Boolean]
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

  def apply(key:Symbol) = elementsMap(key)
  
  override def validate(element: Element) = umatch(element) {
    case m: Model => elements.foreach {
      case (field, telement) => telement.validate(m(field))
    }
  }
}
