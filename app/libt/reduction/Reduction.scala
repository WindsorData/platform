package libt.reduction

import libt._

/**
 * A numeric reduction - aggregation - over an element
 * @author metalkorva
 * */
sealed trait Reduction {
  
  /**Reduces the given element to a BigDecimal*/
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

//TODO generalize this to a custom Reduction
case class SubstractAll(basePath: Path, paths: Path*) extends Reduction{
  override def reduce(model: Element) = {
	  val valuesModel = model(basePath).asModel
	  paths.flatMap(valuesModel(_).asValue[BigDecimal].value)
	  .reduce( (v1, v2) => v1 - v2)
  }
}
