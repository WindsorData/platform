package model

import libt.spreadsheet._
import libt.error._
import libt._

package object mapping {

  val colWrapping = (x:Seq[Validated[Model]]) => Col(x.map(_.get).toList: _*)
  val singleModelWrapping = (x:Seq[Validated[Model]]) => Model(x.head.get.elements)

}