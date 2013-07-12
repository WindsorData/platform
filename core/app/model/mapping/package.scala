package model

import libt.spreadsheet._
import libt.error._
import libt._

package object mapping {

  val colWrapping = (x:Seq[Model]) => Col(x.toList: _*)
  val singleModelWrapping = (x:Seq[Model]) => Model(x.head.elements)

  val docSrcMapping = Seq(Feature(Path('cusip)), Feature(Path('ticker)), Feature(Path('name)))

  def addTYears(paths: Path*): Seq[Strip] =
    paths.flatMap(path => Seq[Strip](
      pathToFeature(path :+ Route('year1)),
      pathToFeature(path :+ Route('year2)),
      pathToFeature(path :+ Route('year3))))
  
}