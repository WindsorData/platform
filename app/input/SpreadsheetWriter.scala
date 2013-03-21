package input

import java.io.InputStream
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.io.OutputStream
import model.Executive
import util.poi.Cells._
import org.apache.poi.ss.usermodel.Workbook
import util.FileManager
import org.apache.poi.ss.usermodel.Cell
import model.Input
import model._
import org.apache.poi.ss.usermodel.Comment
import org.apache.poi.xssf.usermodel.XSSFComment
import org.apache.poi.hssf.usermodel.HSSFPatriarch
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFComment
import org.apache.poi.hssf.usermodel.HSSFClientAnchor
import org.apache.poi.hssf.usermodel.HSSFRichTextString

object SpreadsheetWriter {

  def write(out: OutputStream, company: CompanyFiscalYear) = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x =>
        {
          val wb = WorkbookFactory.create(x)
          writeCompany(wb, company)
          writeExecutives(wb, company)
          wb.write(out)
        }
    }
  }

  def writeComment[T](sheet: Sheet, cell: Cell): Unit = {
    val patr = sheet.createDrawingPatriarch();
    //anchor defines size and position of the comment in worksheet
    val comment1 = patr.createCellComment(new HSSFClientAnchor(100, 100, 100, 100, 1, 1, 6, 5))
    comment1.setString(new HSSFRichTextString("FirstComments"));
    cell.setCellComment(comment1);
  }

  def writeCompany(wb: Workbook, company: CompanyFiscalYear) = {
    val companySheet = wb.getSheet("DOC_SRC")

    val rowsIter = rows(companySheet).drop(2).iterator

    rowsIter.next.getCell(2).setCellValue(company.ticker.value.get)
    rowsIter.next.getCell(2).setCellValue(company.name.value.get)
    rowsIter.next.getCell(2).setCellValue(company.disclosureFiscalYear.value.get)
  }

  def writeExecutives(wb: Workbook, company: CompanyFiscalYear) = {
    val executiveSheet = wb.getSheet("Executives")

    val cellIterators = rows(executiveSheet).drop(3).map(cells).map(_.iterator)
    //Skips first column
    cellIterators.map(_.next)

    def writeValueAndComments[T](value: T, comments: Seq[(String,Option[String])], cell: Cell): Unit = {
      //TODO: don't convert every value toString
      cell.setCellValue(value.toString)
      writeComments(comments, cell)
    }

    def writeComments(comments: Seq[(String, Option[String])], cell: Cell): Unit = {
      val patr = executiveSheet.createDrawingPatriarch();
      val comm = patr.createCellComment(new HSSFClientAnchor(100, 100, 100, 100, 1, 1, 6, 5));
      val stringComment = comments.foldLeft("") { (acc, comment) =>
        comment match {
          case (name, Some(comment)) => acc + name + comment + "\n"
          case (_,None) => acc
        }
      }

      comm.setString(new HSSFRichTextString(stringComment));
      cell.setCellComment(comm);
    }

    //TODO: 
    // Put other input fields as comments for the value
    def getInputValue[T](toSomeValue: Executive => Input[T]) = company.executives.map(toSomeValue).toList

    def writeInputValue[T](names: Traversable[Input[T]], cells: Seq[Cell]): Unit = {
      names match {
        case Input(Some(value), calc, comment, note, link) :: xs => {
          writeValueAndComments(value, Seq(("Calc: ", calc), ("Comment: ", comment), ("Note: ", note), ("Link: ",link)), cells.head)
          writeInputValue(xs, cells.drop(2))
        }
        case Input(None, _, _, _, _) :: xs => writeInputValue(xs, cells.drop(2))
        case Nil => Unit
      }
    }

    def writeCellWithExecutiveValue[T](executive2Input: Executive => Input[T]) =
      writeInputValue(getInputValue(executive2Input), cellIterators.map(_.next))

    writeCellWithExecutiveValue(_.name)
    writeCellWithExecutiveValue(_.title)
    writeCellWithExecutiveValue(_.shortTitle)
    writeCellWithExecutiveValue(_.functionalMatch(1))
    writeCellWithExecutiveValue(_.functionalMatch(2))
    writeCellWithExecutiveValue(_.functionalMatch(3))
    writeCellWithExecutiveValue(_.founder)

    //TODO: write all the 3 years, we are just creating the first one for now.
    writeCellWithExecutiveValue(_.cashCompensations.baseSalary)
    writeCellWithExecutiveValue(_.cashCompensations.actualBonus)
    writeCellWithExecutiveValue(_.cashCompensations.targetBonus)
    writeCellWithExecutiveValue(_.cashCompensations.thresholdBonus)
    writeCellWithExecutiveValue(_.cashCompensations.maxBonus)
    writeCellWithExecutiveValue(_.cashCompensations.new8KData.baseSalary)
    writeCellWithExecutiveValue(_.cashCompensations.new8KData.targetBonus)

    writeCellWithExecutiveValue(_.equityCompanyValue.optionsValue)
    writeCellWithExecutiveValue(_.equityCompanyValue.options)
    writeCellWithExecutiveValue(_.equityCompanyValue.exPrice)
    writeCellWithExecutiveValue(_.equityCompanyValue.bsPercentage)
    writeCellWithExecutiveValue(_.equityCompanyValue.timeVestRsValue)
    writeCellWithExecutiveValue(_.equityCompanyValue.shares)
    writeCellWithExecutiveValue(_.equityCompanyValue.price)
    writeCellWithExecutiveValue(_.equityCompanyValue.perfRSValue)
    writeCellWithExecutiveValue(_.equityCompanyValue.shares2)
    writeCellWithExecutiveValue(_.equityCompanyValue.price2)
    writeCellWithExecutiveValue(_.equityCompanyValue.perfCash)

    writeCellWithExecutiveValue(_.carriedInterest.ownedShares)
    writeCellWithExecutiveValue(_.carriedInterest.vestedOptions)
    writeCellWithExecutiveValue(_.carriedInterest.unvestedOptions)
    writeCellWithExecutiveValue(_.carriedInterest.tineVest)
    writeCellWithExecutiveValue(_.carriedInterest.perfVest)
  }

  def loadTemplateInto(out: OutputStream) = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x => WorkbookFactory.create(x).write(out)
    }
  }
}