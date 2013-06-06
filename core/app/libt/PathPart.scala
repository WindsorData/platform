package libt

import libt.util.Symbols.richWord
import libt.util._

/**
 * A single part in a Path.
 *
 * Path parts are the smallest unit of a Path
 * @author flbulgarelli
 */
sealed trait PathPart {
  def routeValue = umatch(this) {
    case Route(value) => value
  }
  /**A pretty print string representation
   * of this PathPart*/
  def name : String
}
/**A path part that points to a field in a Model */
case class Route(symbol: Symbol) extends PathPart {
  override def name = symbol.upperCaseFromCamelCase
}

/**A path part that points to an element of a Col*/
case class Index(position: Int) extends PathPart {
  override def name = position.toString
}
/**A wilcard path part. This PathPart has no specific semantic, it's actual meaning 
 * depends on the context*/
case object * extends PathPart {
  def name = "*"
}
