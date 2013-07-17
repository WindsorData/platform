package libt.spreadsheet.reader

import MyWorkbookFactory._
import libt._
import libt.spreadsheet._
import org.scalatest.FlatSpec


class WorkbookReaderSpec extends FlatSpec {

  it should "be able to read a single column oriented input without combining" in {
    val workbook = createNewSingleSheetWorkbook.addSingleColumnOrientedValue(2, "a")
    val mapping = WorkbookMapping(Seq(Area(TModel('a -> TString), Offset(0, 2), None, ColumnOrientedLayout(WithMetadataValueReader), Seq(Feature(Path('a))))))
    assert(mapping.read(workbook).get.head === Seq(Model('a -> Value("a"))))
  }

  it should "be able to read two column oriented inputs without combining" in {
    val workbook = createNewSingleSheetWorkbook
      .addSingleColumnOrientedValue(0, "a")
      .addSingleColumnOrientedValue(1, "b")
    val mapping = WorkbookMapping(
      Seq(
        Area(
          TModel('a -> TString, 'b -> TString),
          Offset(0, 0),
          None,
          ColumnOrientedLayout(WithMetadataValueReader),
          Seq(Feature(Path('a)), Feature(Path('b))))))
    assert(mapping.read(workbook).get.head === Seq(Model('a -> Value("a"), 'b -> Value("b"))))
  }

  it should "be able to read row oriented inputs" in {
    val workbook = createNewSingleSheetWorkbook.addSingleRowOrientedValue(1, "a")
    val mapping = WorkbookMapping(Seq(Area(TModel('a -> TString), Offset(1, 0), None, RowOrientedLayout(WithPartialMetadataValueReader), Seq(Feature(Path('a))))))

    assert(mapping.read(workbook).get.head === Seq(Model('a -> Value("a"))))
  }
}