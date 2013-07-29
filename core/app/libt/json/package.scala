package libt

import play.api.libs.json._
import play.api.libs.json.Json._

package object json {
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
        case Value(value, _, _, _, _) => value
        case Model(elements) => toJson(elements.map {
          case (key, value) => key.name -> elementToJson(value)
        }.toMap)
        case Col(elements@_*) => toJson(elements.map(elementToJson))
      }

    def asJson = elementToJson(element)
  }
}
