package persistence

import com.mongodb.casbah.Imports._
import libt.persistence._
import libt._

trait Persistence {
  type DBO = DBObject
  val TDBSchema: TModel
  protected val colName: String
  protected val pk: Seq[Path]

  def marshall = TDBSchema.marshall(_)
  def unmarshall = TDBSchema.unmarshall(_)

  protected def collection(implicit db: MongoDB) = db(colName)

  implicit def mongoCollection2Models(result: MongoCollection#CursorType): Seq[Model] =
    result.toSeq.map(unmarshall(_).asModel)

  def operatorExpression(property: String, operator: String, values: Any) =
    MongoDBObject(property -> MongoDBObject(operator -> values))

  def insert(models: Model*)(implicit db: MongoDB) = {
    models.foreach(model => collection.insert(TDBSchema.marshall(model)))
  }

  def singlePKQuery(model: Model) =
    MongoDBObject(
      pk.map { path =>
        key(path) -> model(path).getRawValue[Any]
      }:_*)

  def key(path: Path) : String = (path ++ Path('value)).joinWithDots

  def update(models: Model*)(implicit db: MongoDB) = {
    models.foreach{ model =>
      collection.update(
        singlePKQuery(model),
        MongoDBObject("$set" -> TDBSchema.marshall(model)),true)
    }
  }

  def find(query: DBO)(implicit db: MongoDB): Seq[Model] = collection.find(query)

  def findAll(implicit db: MongoDB): Seq[Model] = collection.find

  def findAllMap[A](mapper: Model => A)(implicit db: MongoDB): Seq[A] =
    findAll.map(mapper).toSet.toSeq

  def clean(implicit db: MongoDB) = collection.drop()

  def drop(implicit db: MongoDB) = db.dropDatabase()
}
