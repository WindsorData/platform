import play.api.libs.json._
import play.api.libs.json.Json._

package object libt {

  /**
   * A path is a pointer to an element inside another element.
   */
  type Path = List[PathPart]

  /* Small DSL for declaring paths  */

  implicit def symbol2Route(s: Symbol) = Route(s)

  implicit def int2Index(i: Int) = Index(i)

  implicit def path2RichPath(path: Path) = new {
    def titles = path.map(_.name)
    def joinWithDots = path.map(_.routeValue.name).mkString(".")
  }

  implicit def model2RichModel(element: Element) = new {
    implicit def valueToJsonValue(value: Option[Any]): JsValue = value match {
      case None => JsNull
      case Some(v: String) => JsString(v)
      case Some(v: BigDecimal) => JsNumber(v)
      case Some(v: Double) => JsNumber(v)
      case Some(v: Int) => JsNumber(v)
      case Some(v: Boolean) => JsBoolean(v)
    }

    def elementToJson(element: Element): JsValue =
      element match {
        case Value(value,_,_,_,_) => value
        case Model(elements) => toJson(elements.map { case (key, value) => key.name -> elementToJson(value) }.toMap)
        case Col(elements@_*) => toJson(elements.map(elementToJson))
      }

    def asJson = elementToJson(element)
  }

  object Path {
    def apply(parts: PathPart*): Path = List(parts: _*)
  }

  def Relative(base: Path, relativePaths: Path*) = relativePaths.map {base ++ _}

  /**
   * A PK is just a sequence of Paths that represent
   * the primary identifier an element
   */
  //TODO rename
  type PK = Seq[Path]

  object PK {
    def apply(elements: Path*) : PK = elements
  }
}