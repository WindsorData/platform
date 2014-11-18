package persistence

import com.mongodb.casbah.Imports._
import libt.persistence._
import libt._

/**
 * Trait that describes a DB
 */
//FIXME rename to DB
trait Persistence {
  val TDBSchema: TModel
  protected val colName: String
  protected val pk: Seq[Path]
  protected val db: MongoDB

  def marshall = TDBSchema.marshall(_)
  def unmarshall = TDBSchema.unmarshall(_)

  protected def collection = db(colName)

  implicit def mongoCollection2Models(result: MongoCollection#CursorType): Seq[Model] =
    result.toSeq.map(unmarshall(_).asModel)

  def operatorExpression(property: String, operator: String, values: Any) =
    MongoDBObject(property -> MongoDBObject(operator -> values))

  def insert(models: Model*) = {
    models.foreach(model => collection.insert(TDBSchema.marshall(model)))
  }

  def singlePKQuery(model: Model) =
    MongoDBObject(
      pk.map { path =>
        key(path) -> model(path).getRawValue[Any]
      }:_*)

  def key(path: Path) : String = (path ++ Path('value)).joinWithDots

  def update(models: Model*) = {
    models.foreach{ model =>
      collection.update(
        singlePKQuery(model),
        MongoDBObject("$set" -> TDBSchema.marshall(model)),true)
    }
  }

  def find(query: DBO): Seq[Model] = collection.find(query)

  def findWith(query: DBO, projection: DBO): Seq[Model] = collection.find(query, projection)

  def findAll: Seq[Model] = collection.find

  def findAllWith(projection: DBO): Seq[Model] = collection.find(MongoDBObject(),projection)

  def findAllMap[A](mapper: Model => A): Seq[A] =
    findAll.map(mapper).toSet.toSeq

  def drop = collection.drop()
}
