package libt
package object spreadsheet {
  implicit def columnsSeq2RichColummnsSeq(columns: Seq[Strip]) = new {
    /**Iterates over the {{WriteOps}} of each column */
    def foreachWithOps(model: Element, schema: TElement)(action: WriteOps => Unit) =
      columns.foreach { column =>
        val ops = column.writeOps(schema, model)
        action(ops)
      }
  }
  
}