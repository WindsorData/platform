package output.calculation

import libt._

sealed trait Reduction {
  def reduce(model: Element): BigDecimal
  protected def filterValues(path: Path, model: Element) =
    model.applySeq(path).map(_.asValue[BigDecimal])
}

case class Sum(path: Path) extends Reduction {
  override def reduce(model: Element) =
    filterValues(path, model).flatMap(_.value).sum
}

case class Average(path: Path) extends Reduction {
  override def reduce(model: Element) = 
	Sum(path).reduce(model) / filterValues(path, model).size 
}

case class CustomReduction(
    basePath: Path, 
    reductionMapper: Model => BigDecimal) extends Reduction {
  override def reduce(model: Element) = reductionMapper(model(basePath).asModel)
}
