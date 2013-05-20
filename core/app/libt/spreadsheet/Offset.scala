package libt.spreadsheet
//TODO rename to delta
case class Offset(rowIndex: Int, columnIndex: Int) {
  def +(that: Offset) =
    Offset(rowIndex + that.rowIndex, columnIndex + that.columnIndex)
}