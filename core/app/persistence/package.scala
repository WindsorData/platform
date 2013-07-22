import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoClient

package object persistence {
  type DBO = DBObject

  object ExecutivesDb extends CompaniesDb(MongoClient()("windsor"))
  object BodDb extends CompaniesDb(MongoClient()("windsor-bod"))
  object PeersDb extends PeersCompaniesDb(MongoClient()("windsor-peers"))
}
