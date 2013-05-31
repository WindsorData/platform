package model

import libt.spreadsheet._
import libt.error._
import libt._

package object mapping {

  implicit def pathToFeature(path: Path): Feature = Feature(path)

  def colOfModelsPath(basePath: Path, times: Int, paths: Symbol*): Seq[Strip] =
    for (index <- 0 to times; valuePath <- paths) yield Feature((basePath :+ Index(index)) :+ Route(valuePath))
  
  val colWrapping = ((x:Seq[Validated[Model]]) => Col(x.map(_.get).toList: _*))
  val singleModelWrapping = ((x:Seq[Validated[Model]]) => Model(x.head.get.elements))

}