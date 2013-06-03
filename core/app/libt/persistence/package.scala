package libt

package object persistence {

  import com.mongodb.DBObject
  import com.mongodb.casbah.commons.MongoDBObject
  import com.mongodb.casbah.Imports._
  import com.mongodb.casbah.commons.MongoDBList
  import libt._

  type DBO = DBObject

  implicit def tElement2PersitentTElement(telement: TElement): TElementConverter = telement match {
    case TEnum(v, _) => new TValueConverter(v)
    case v: TValue[_] => new TValueConverter(v)
    case m: TModel => new TModelConverter(m)
    case c: TCol => new TColConverter(c)
  }
}
