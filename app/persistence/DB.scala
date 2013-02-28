package persistence

import org.squeryl.{ Schema, KeyedEntity }
import model.Company

object DB extends Schema {
  val companies = table[Company]("companies")
}