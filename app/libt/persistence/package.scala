package libt.persistence

import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBList
import libt._

object `package` {

  type DBO = DBObject

  implicit def tElement2PersitentTElement(telement: TElement): TElementConverter = telement match {
    case _: TValue => TValueConverter
    case m: TModel => new TModelConverter(m)
    case c: TCol => new TColConverter(c)
  }

  trait TElementConverter {
    def marshall(it: Element): DBO
    def unmarshall(it: DBO): Element
  }

  class TColConverter(tCol: TCol) extends TElementConverter {
    def marshall(it: Element): DBO = 
      MongoDBList(it.asInstanceOf[Col].elements.map(marshallColElement): _*)
    
    def unmarshall(it: DBO): Element =
      Col(it.asInstanceOf[BasicDBList].view.map(_.asInstanceOf[DBO]).map(unmarshallColElement): _*)
      
    private val marshallColElement = tCol.element.marshall(_)
    private val unmarshallColElement = tCol.element.unmarshall(_)
  }

  class TModelConverter(tModel: TModel) extends TElementConverter {
    def marshall(it: Element) = {
      val model = it.asInstanceOf[Model]
      MongoDBObject(
        tModel.elements.map {
          case (key, telement) =>
            (key.toString -> telement.marshall(model(key)))
        }: _*)
    }

    def unmarshall(it: DBO) =
      Model(tModel.elements.map {
        case (key, telement) =>
          (key -> telement.unmarshall(it(key.toString).asInstanceOf[DBO]))
      }: _*)
  }

  object TValueConverter extends TElementConverter {
    def marshall(it: Element) = {
      val value = it.asInstanceOf[Value[_]]
      MongoDBObject(
        "value" -> value.value,
        "calc" -> value.calc,
        "comment" -> value.comment,
        "note" -> value.note,
        "link" -> value.link)
        .filter {
          case (k, v) => v != null
        }
    }

    def unmarshall(it: DBO): Element = Value(
      Option(it.get("value")),
      Option(it.get("calc").asInstanceOf[String]),
      Option(it.get("comment").asInstanceOf[String]),
      Option(it.get("note").asInstanceOf[String]),
      Option(it.get("link").asInstanceOf[String]))
  }
}