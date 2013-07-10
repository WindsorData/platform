package libt
import libt.util._
import java.util.Date

/**
 * Type mirror of Element. It is
 * the base trait for defining a data schema
 *
 * @author flbulgarelli
 */
sealed trait TElement extends ElementLike[TElement] {
  override type ModelType = TModel
  override type ColType = TCol
  override type ValueType[A] = TValue[A]
  
  /**
   * Validates the given element using this TElement as schema,
   * by failing if it does not conform to this TElement
   */
  def validate(element: Element) = ()
}

/*=======Value=======*/

/**
 * Type mirror of Value
 * @author flbulgarelli
 */
sealed trait TValue[A] extends TElement with ValueLike[TElement, A]
case object TString extends TValue[String]
case object TAny extends TValue[String]
case object TInt extends TValue[Int]
case object TBool extends TValue[Boolean]
case object TXBool extends TValue[Boolean]
case object TDate extends TValue[Date]
case object TNumber extends TValue[BigDecimal]

case class TEnum[A](valueType: TValue[A], values: Seq[A]) extends TValue[A] {
  
  private val valuesSet = values.toSet
  
  private def isValue = valuesSet.contains(_)
  
  override def validate(element: Element) = umatch(element) {
    case v: Value[A] => assert(v.value.forall(isValue(_)))
  }
}

object TStringEnum {
  def apply(values: String*) = TEnum(TString, values)
}

object TNumberEnum {
  def apply(values: BigDecimal*) = TEnum(TNumber, values)
}

/*=======Col=======*/

/**
 * Type mirror of Col
 * @author flbulgarelli
 */
case class TCol(elementType: TElement)
  extends TElement
  with ColLike[TElement] {
  
  override def apply(index: Int) = elementType
  
  override def validate(element: Element) = umatch(element) {
    case c: Col => c.elements.foreach(elementType.validate(_))
  }
}

/*=======Model=======*/

/**
 * Type mirror of Model
 * @author flbulgarelli
 */
case class TModel(elementTypes: (Symbol, TElement)*)
  extends TElement
  with ModelLike[TElement]
  with ModelLikeOps[TElement] {
  private val elementsMap = elementTypes.toMap

  override def get(key: Symbol) = elementsMap.get(key)

  override def validate(element: Element) = umatch(element) {
    case m: Model => elementTypes.foreach {
      case (field, telement) => telement.validate(m(field))
    }
  }
  
  def keys = elementTypes.map(_._1)
}
