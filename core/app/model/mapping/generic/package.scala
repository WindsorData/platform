package model.mapping

import libt._

package object generic {
  type RowNumber = Int
  type Year = Model
  type SheetPointer[PointerType] = (PointerType, Symbol, ElementCombiner)
  type ElementCombiner = Seq[Model] => Element
}
