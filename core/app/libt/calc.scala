package libt

import javax.script.{ScriptException, ScriptEngineManager}

package object calc {
  val engine = new ScriptEngineManager().getEngineByName("javascript")

  case class Calc(value: String) {
    def formula = value.dropWhile(c => c.isWhitespace || c == '=')

    def apply() = try {
      Some(engine.eval(formula))
    } catch { case e: ScriptException =>
      None
    }
  }

  implicit class ValueCalcOps(val self: Value[BigDecimal]) {
    def round(number: BigDecimal) = number.setScale(2, BigDecimal.RoundingMode.HALF_UP)
    def isConsistent =
      (for (v <- self.value; c <- self.calc; result = Calc(c)())
        yield result.nonEmpty &&
              round(v) == round(result.get.asInstanceOf[Double])).getOrElse(true)
  }

}
