package model.mapping.generic


import _root_.util.WorkbookLogger.ReaderError
import libt.spreadsheet.reader.workflow._
import libt.spreadsheet.reader._
import libt.spreadsheet.util._
import libt.error._
import libt._

import scala.collection.JavaConversions._
import org.apache.poi.ss.usermodel.{Cell, Row, Sheet, Workbook}
import org.joda.time.DateTime
import java.util.Date

trait DocSrcModelCombiner {
  def combineModels(pointers: Seq[SheetPointer[Validated[Year]]], models: Seq[Seq[Model]], docSrcModel: Model): Validated[Seq[Model]]
}

/**
 * *
 * Windsor worksheets specific combiner, that assumes that the excel file
 * has as first sheet a DOC_SRC area, which has year metadata, that should be zipped
 * with the rest of the sheets.
 *
 * For instance, when a DOC_SRC states that there are three years  - 2008, 2009, and 2010 -
 * then this combiner will look for up to 3 other sheets with data, and zip them with those years
 *
 * @author mcorbanini
 */
trait DocSrcCombiner
  extends Phase[Seq[Seq[Model]], Seq[Model]] {
  self: DocSrcModelCombiner =>

  val rowPointers: Seq[SheetPointer[RowNumber]]

  protected def yearPointers(wb: Workbook): Seq[SheetPointer[Validated[Year]]] =
    yearsPointersBySheet(wb.getSheetAt(0)).filter(!_._1.isInvalid)

  def yearsPointersBySheet(sheet: Sheet) =
    rowPointers.map {
      case (yearIndex, key, elemWrap) => (fiscalYearAndMetadata(sheet.rows.drop(yearIndex)), key, elemWrap)
    }

  def fiscalYearAndMetadata(r: Seq[Row]): Validated[Model] = {
    def dateCellToValue(dateCell: Cell): Validated[DateTime] =
      try {
        Valid(new DateTime(blankToNone(_.getDateCellValue)(dateCell).get))
      } catch {
        case e: NoSuchElementException =>
          Invalid(ReaderError().noFiscalYearProvidedAt(dateCell))
        case e: RuntimeException =>
          Invalid(ReaderError(e.getMessage()).description(dateCell))
      }

    dateCellToValue(r.get(0).getCell(2)).map { date =>
      Model(
        'disclosureFiscalYear ->
          Value(
            Some(date.getYear),
            blankToNone(_.getStringCellValue)(r.get(0).getCell(3)),
            blankToNone(_.getStringCellValue)(r.get(0).getCell(4))),
        'disclosureFiscalYearDate -> Value(date.toDate),
        'def14a ->
          Value(
            blankToNone(_.getDateCellValue)(r.get(1).getCell(2)),
            blankToNone(_.getStringCellValue)(r.get(1).getCell(3)),
            blankToNone(_.getStringCellValue)(r.get(1).getCell(4))),
        'tenK ->
          Value(
            blankToNone(_.getDateCellValue)(r.get(2).getCell(2)),
            blankToNone(_.getStringCellValue)(r.get(2).getCell(3)),
            blankToNone(_.getStringCellValue)(r.get(2).getCell(4))),
        'otherDocs -> Col((4 to 12).filterNot(_ == 8).map { i =>
          Model(
            'type -> Value(blankToNone(_.getStringCellValue)(r.get(i).getCell(1)), None, None),
            'date ->
              Value(
                blankToNone(_.getDateCellValue)(r.get(i).getCell(2)),
                blankToNone(_.getStringCellValue)(r.get(i).getCell(3)),
                blankToNone(_.getStringCellValue)(r.get(i).getCell(4))))
        }: _*))
    }

  }

  override def apply(wb: Workbook, results: Seq[Seq[Model]]): Validated[Seq[Model]] = {
    val yearsWithKeys = yearPointers(wb)
    yearsWithKeys.concatMap(_._1) andThen {
      combineModels(yearsWithKeys, results.tail, results.head.head)
    }
  }
}

trait StandardDocSrcModelCombiner extends DocSrcModelCombiner {
  def combineModels(pointers: Seq[SheetPointer[Validated[Year]]], models: Seq[Seq[Model]], docSrcModel: Model) =
    (pointers, models).zipped.map {
      case ((year, key, elemWrap), executives) =>
        year.map {
          it => it ++ docSrcModel ++ Model(key -> elemWrap(executives))
        }
    }.concat
}

case class StandardDocSrcCombiner(override val rowPointers: SheetPointer[RowNumber]*)
  extends DocSrcCombiner with StandardDocSrcModelCombiner
