package libt.util

object Strings {

  implicit def richWord(aString : String) = new {


    /**
     * Convert a word in camelcase into words
     * split by spaces in uppercase
     *
     * {{{ "pathPart".upperCaseFromCamelCase  = "Path Part" }}}
     */
    def upperCaseFromCamelCase() = aString.foldLeft("") {
      (acc, ch) =>
        if (ch.isUpper) acc ++ " " ++ Seq(ch)
        else acc ++ Seq(ch)
    }.capitalize
  }

}
