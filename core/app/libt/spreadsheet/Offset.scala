package libt.spreadsheet
//TODO rename to delta
case class Offset(rowIndex: Int, columnIndex: Int) {
  /**Sums both row and column offsets of this offset and other one*/
  def +(that: Offset) =
    Offset(rowIndex + that.rowIndex, columnIndex + that.columnIndex)
}