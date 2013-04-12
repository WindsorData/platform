package libt

sealed trait TElement

sealed trait TValue extends TElement
case object TString extends TValue
case object TNumber extends TValue
case class TEnum(values: String*) extends TValue

case class TCol(element: TElement) extends TElement
case class TModel(elements: (Symbol, TElement)*) extends TElement


//.asInstanceOf[BasicDBList].map(x => dbObject2Executive(x.asInstanceOf[DBO]))
//    functionalMatches.toSet[Input[String]].flatMap { _.value }.subsetOf(Executive.functionalMatchValues)
