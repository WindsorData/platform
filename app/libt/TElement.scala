package libt

sealed trait TElement {
  def apply(path: Path): TValue
  def validate(elemnt: Element) = ()
}

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

case class TCol(tElement: TElement) extends TElement {
  override def apply(path: Path) = path match {
    case Index(_) :: tail => tElement(tail)
  }
  override def validate(element: Element) = element match {
    case c: Col => c.elements.foreach(tElement.validate(_))
  }
}

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


//.asInstanceOf[BasicDBList].map(x => dbObject2Executive(x.asInstanceOf[DBO]))
//    functionalMatches.toSet[Input[String]].flatMap { _.value }.subsetOf(Executive.functionalMatchValues)
