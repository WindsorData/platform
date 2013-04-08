package libt.persistence

import org.bson.BSON
import org.bson.Transformer
package object util {
  def registerBigDecimalConverter() {
    BSON.addEncodingHook(classOf[BigDecimal], new Transformer() {
      def transform(o: AnyRef) = {
        o.toString
      }
    })
  }
}