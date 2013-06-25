package model

import libt.error._
import libt._

package object validation {

  def execMsg(year: Int, m: Model) =
    year + " - " + m(Path('firstName)).getRawValue[String] + m(Path('lastName)).getRawValue[String] + " - "

  def reduceExecutiveValidations(path: Path, model: Model)(action: (Element) => Validated[Model]) = {
    val results: Seq[Validated[Model]] = model.applySeq(path).map { m => action(m) }
    results.reduce((a, b) => a andThen b)
  }

  def threeDigitValidation(basePath: Path, valuePaths: Seq[Path], model: Model) =
    reduceExecutiveValidations(basePath, model)(
      (m) => {
        val results: Seq[Validated[Model]] =
          valuePaths.map(path => m(path).rawValue[BigDecimal] match {
            case Some(salary) if salary.compare(BigDecimal(100)) < 0 =>
              Doubtful(model,
                "Warning on ExecDb " + execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], m.asModel) +
                  ": " + valuePaths.map(_.titles).mkString(" - ") + " should be 3 digits or more")
            case _ => Valid(model)
          })
        results.reduce((a, b) => a andThen b)
      })
}