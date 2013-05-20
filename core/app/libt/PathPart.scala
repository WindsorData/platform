package libt
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
  override def name = symbol.name.foldLeft("") {
    (acc, ch) =>
      if (ch.isUpper) acc ++ " " ++ Seq(ch) 
      else acc ++ Seq(ch)   
  }.capitalize
}
/**A path part that points to an element of a Col*/
case class Index(position: Int) extends PathPart {
  override def name = position.toString
}
/**A path part that points to the elements of a Col*/
case object * extends PathPart {
  def name = "*"
}
