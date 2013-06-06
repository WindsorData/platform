package libt.util

object Symbols {

  implicit def richWord(symbol : Symbol) = new {


    /**
     * Convert a word in camelcase into words
     * split by spaces in uppercase
     *
     * "pathPart".fromCamelCase       = "Path Part"
     */
    def upperCaseFromCamelCase() = symbol.name.foldLeft("") {
      (acc, ch) =>
        if (ch.isUpper) acc ++ " " ++ Seq(ch)
        else acc ++ Seq(ch)
    }.capitalize
  }

}
