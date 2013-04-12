package libt

sealed trait Element {
  def m(key : Symbol) = this.asInstanceOf[Model](key)
  def v[A](key : Symbol) = m(key).asInstanceOf[Value[A]] 
  def c(key : Symbol) = m(key).asInstanceOf[Col].elements
  def mc(key : Symbol)(pos : Int) = c(key)(pos).asInstanceOf[Model]
  def vc[A](key : Symbol)(pos : Int) = c(key)(pos).asInstanceOf[Value[A]]
}

//extends Dynamic {
//  def selectDynamic(key : String) : Element = this match {
//    case m : Model => m(Symbol(key))
//  } 
//  
//  def value[A] : Option[A] = this match {
//    case v : Value[A] => v.value
//  }
//    
//}

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
}
object Model {
  def apply(elements : (Symbol, Element)*) : Model = Model(elements.toSet)
}

