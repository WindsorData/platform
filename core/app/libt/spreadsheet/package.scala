package libt
package object spreadsheet {
  
  /**Syntactic sugar for making mappings declarations more concise*/
  implicit def pathToFeature(path: Path) = Feature(path)
  
  implicit def columnsSeq2RichColummnsSeq(columns: Seq[Strip]) = new {
    /**Iterates over the {{WriteOps}} of each column */
    def foreachWithOps(model: Element, schema: TElement)(action: WriteOps => Unit) =
      columns.foreach { column =>
        val ops = column.writeOps(schema, model)
        action(ops)
      }
  }
  
}