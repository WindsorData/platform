package libt

import javax.script.{ScriptException, ScriptEngineManager}

package object calc {
  val engine = new ScriptEngineManager().getEngineByName("javascript")

  case class Calc(value: String) {
    def formula = value.dropWhile(c => c.isWhitespace || c == '=')

    def apply() = engine.eval(formula)
  }

  implicit class ValueCalcOps(val self: Value[BigDecimal]) {
    def isConsistent =
      (for (v <- self.value; c <- self.calc)
        yield v == BigDecimal(Calc(c)().asInstanceOf[Double]).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      .getOrElse(true)
  }

}
