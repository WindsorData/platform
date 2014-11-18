package windsor

import windsor.persistence.CompaniesDb;
import windsor.persistence.PeersCompaniesDb;

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoClient

/**
 * Listing of the Windsor Data DB's
 */
package object persistence {
  object ExecutivesDb extends CompaniesDb(MongoClient()("windsor"))
  object BodDb extends CompaniesDb(MongoClient()("windsor-bod"))
  object PeersDb extends PeersCompaniesDb(MongoClient()("windsor-peers"))
}
