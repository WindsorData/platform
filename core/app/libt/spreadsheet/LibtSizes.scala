package libt.spreadsheet

/**Sizes of the meta model, in terms of number of spreadsheet cells*/
trait LibtSizes {

  /**Size of a whole {{libt.Value}}*/
  val ValueSize = MetadataSize + 1
  /**Size of metadata fields of a {{libt.Value}}*/
  val MetadataSize = 4
  /**Size of the titles seq of a {{libt.spreadsheet.Feature}}*/
  val TitlesSize = 2

}