package util

import org.bson.BSON
import org.bson.Transformer
package object persistence {
  def registerBigDecimalConverter() {
    BSON.addEncodingHook(classOf[BigDecimal], new Transformer() {
      def transform(o: AnyRef) = {
        o.toString
      }
    })
  }
}