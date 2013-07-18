package libt.util

package object math {
  implicit class RichDecimal(val self: BigDecimal) {
    def roundUp(scale: Int) = self.setScale(scale, BigDecimal.RoundingMode.HALF_UP)
  }
}