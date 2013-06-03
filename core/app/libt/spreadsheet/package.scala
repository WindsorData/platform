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

  /**
   * Answers a Seq of strips that are relative to a base path that point to a Col.
   *
   * The returned Seq contains {{{count * relativePaths.size}}} strips
   *
   * @param base a base path that points to a collection
   * @param count the number of elements from the collection to map
   * @param relativePaths the paths of the actual Feature strips, relative to the basePath and each index
   */
  def Multi(base: Path, count: Int, relativePaths: Path*) =
    for (index <- 0 to count; relativePath <- relativePaths)
      yield Feature((base :+ Index(index)) ++ relativePath)

}