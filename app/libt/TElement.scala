package libt

sealed trait TElement {
  def apply(path: Path): TValue
}

sealed trait TValue extends TElement {
  override def apply(path: Path) =  path match {
    case Nil => this
  }
}
case object TString extends TValue
case object TInt extends TValue
case object TBool extends TValue
case object TDate extends TValue
case object TNumber extends TValue
case class TEnum(values: String*) extends TValue

case class TCol(element: TElement) extends TElement {
  override def apply(path: Path) = path match {
    case Index(_) :: tail => element(tail)
  }
}

case class TModel(elements: (Symbol, TElement)*) extends TElement {
  private val elementsMap = elements.toMap

  override def apply(path: Path) = path match {
    case Route(field) :: tail => elementsMap(field)(tail)
  }
}


//.asInstanceOf[BasicDBList].map(x => dbObject2Executive(x.asInstanceOf[DBO]))
//    functionalMatches.toSet[Input[String]].flatMap { _.value }.subsetOf(Executive.functionalMatchValues)
