package libt

import javax.script.{ScriptException, ScriptEngineManager}
import libt.error.generic._

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
    def isConsistent =
      (for (v <- self.value; c <- self.calc; result = Calc(c)())
        yield result.nonEmpty &&
              v == BigDecimal(result.get.asInstanceOf[Double]).setScale(2, BigDecimal.RoundingMode.HALF_UP)).getOrElse(true)
  }

}
