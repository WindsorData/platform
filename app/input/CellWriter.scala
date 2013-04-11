package input

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.hssf.usermodel.HSSFClientAnchor
import org.apache.poi.hssf.usermodel.HSSFRichTextString
import libt._
import libt.spreadsheet.util._

class ColumnOrientedWriter(sheet: Sheet, company: Model) {

  val cellIterators = sheet.rows.drop(3).map(_.cells).map(_.iterator)
  //Skips first column
  cellIterators.map(_.next)

  def writeValueAndComments[T](value: T, comments: Seq[(String, Option[String])], cell: Cell): Unit = {
    //TODO: don't convert every value toString
    cell.setCellValue(value.toString)
    writeComments(comments, cell)
  }

  def writeComments(comments: Seq[(String, Option[String])], cell: Cell): Unit = {
    val patr = sheet.createDrawingPatriarch();
    val comm = patr.createCellComment(new HSSFClientAnchor(100, 100, 100, 100, 1, 1, 6, 5));
    val stringComment = comments.foldLeft("") { (acc, comment) =>
      comment match {
        case (name, Some(comment)) => acc + name + comment + "\n"
        case (_, None) => acc
      }
    }

    comm.setString(new HSSFRichTextString(stringComment));
    cell.setCellComment(comm);
  }

  //TODO: 
  // Put other input fields as comments for the value
  def getInputValue[T](toSomeValue: Model => Value[T]) =
    company.c('executives).map(_.asInstanceOf[Model]).map(toSomeValue).toList

  def writeInputValue[T](names: Traversable[Value[T]], cells: Seq[Cell]): Unit = {
    names match {
      case Value(Some(value), calc, comment, note, link) :: xs => {
        writeValueAndComments(value, Seq(("Calc: ", calc), ("Comment: ", comment), ("Note: ", note), ("Link: ", link)), cells.head)
        writeInputValue(xs, cells.drop(2))
      }
      case Value(None, _, _, _, _) :: xs => writeInputValue(xs, cells.drop(2))
      case Nil => Unit
    }
  }

  def writeString[T](executive2Input: Model => Value[T]) =
    writeInputValue(getInputValue(executive2Input), cellIterators.map(_.next))

}