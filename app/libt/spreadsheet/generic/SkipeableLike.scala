package libt.spreadsheet.generic

trait SkipeableLike {
  def skip(offset: Int) : Unit = for (_ <- 1 to offset) skip1
  protected def skip1: Unit
}