package util.persistence

import org.squeryl.KeyedEntity

class LongKeyedEntity extends KeyedEntity[Long] {
  val id: Long = 0
}
