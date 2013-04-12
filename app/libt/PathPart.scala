package libt

/**
 * A single part in a Path.
 *
 * Path parts are the smallest unit of a Path
 * @author flbulgarelli
 */
sealed trait PathPart
/**A path part that points to a field in a Model */
case class Route(symbol: Symbol) extends PathPart
/**A path part that points to an element of a Col*/
case class Index(position: Int) extends PathPart
  