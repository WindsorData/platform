package libt.persistence

import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBList
import libt._

trait TElementConverter {
  def marshall(it: Element): DBO
  def unmarshall(it: DBO): Element
}

class TColConverter(tCol: TCol) extends TElementConverter {
  def marshall(it: Element): DBO =
    MongoDBList(it.asInstanceOf[Col].elements.map(marshallColElement): _*)

  def unmarshall(it: DBO): Element =
    Col(it.asInstanceOf[BasicDBList].view.map(_.asInstanceOf[DBO]).map(unmarshallColElement): _*)

  private val marshallColElement = tCol.elementType.marshall(_)
  private val unmarshallColElement = tCol.elementType.unmarshall(_)
}

class TModelConverter(tModel: TModel) extends TElementConverter {
  def marshall(it: Element) = {
    val model = it.asInstanceOf[Model]
    MongoDBObject(
      tModel.elementTypes.map {
        case (key, telement) =>
          (key.toString -> telement.marshall(model(key)))
      }: _*)
  }

  def unmarshall(it: DBO) =
    Model(tModel.elementTypes.map {
      case (key, telement) =>
        (key -> telement.unmarshall(it(key.toString).asInstanceOf[DBO]))
    }: _*)
}

class TValueConverter(v: TValue[_]) extends TElementConverter {
  def marshall(it: Element) = {
    val value = it.asInstanceOf[Value[_]]
    
    def convert(v: Option[_]) = v match {
      case Some(value : BigDecimal) => Some(value.toString)
      case _ => v
    }
    
    MongoDBObject(
      "value" -> convert(value.value),
      "calc" -> value.calc,
      "comment" -> value.comment,
      "note" -> value.note,
      "link" -> value.link)
      .filter {
        case (k, v) => v != null
      }
  }

  def unmarshall(it: DBO): Element = Value(
    convert(it.get("value")),
    Option(it.get("calc").asInstanceOf[String]),
    Option(it.get("comment").asInstanceOf[String]),
    Option(it.get("note").asInstanceOf[String]),
    Option(it.get("link").asInstanceOf[String]))

  def convert(value: AnyRef) = v match {
    case TNumber => Option(value).map {
      it => BigDecimal(it.asInstanceOf[String])
    }
    case _ => Option(value)
  }
}