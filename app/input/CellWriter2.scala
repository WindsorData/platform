package input

import util.poi.Cells._
import org.apache.poi.ss.usermodel.Sheet
import model.Input
import org.apache.poi.ss.usermodel.Cell

class CellWriter(sheet: Sheet) {
  val cellIterators = rows(sheet).map(cells).map(_.iterator)

  def getSheet = sheet
  def skip1 = cellIterators.foreach(_.next)
  def next = cellIterators.map(_.next)

  def string(input: Input[String]) = writeInputValue(input, next)
  def numeric(input: Input[BigDecimal]) = writeInputValue(input, next)

  def writeInputValue[T](input: Input[T], cells: Seq[Cell]): Unit = {
    input match {
      case Input(Some(value), calc, comment, note, link) => {
        val iter = cells.iterator
        iter.next.setCellValue(value.toString)
        Seq(calc, comment, note, link).foreach{
          _ match {
          	case Some(metaValue) => iter.next.setCellValue(metaValue)
          	case None => iter.next
          }
        }
      }
      case Input(None, _, _, _, _) => skip1
    }
  }

}