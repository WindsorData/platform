package libt

import javax.script.ScriptEngineManager

package object calc {
  val engine = new ScriptEngineManager().getEngineByName("javascript")

  case class Calc(value: String) {
    def formula = value.dropWhile(c => c.isWhitespace || c == '=')

    def apply() = engine.eval(formula)
  }

  implicit class ValueCalcOps(val self: Value[_]) {
    def isConsistent =
      (for (v <- self.value; c <- self.calc) yield v == Calc(c)()).getOrElse(true)
  }

}
