package libt

sealed trait PathPart
case class Route(symbol: Symbol) extends PathPart
case class Index(position: Int) extends PathPart
  