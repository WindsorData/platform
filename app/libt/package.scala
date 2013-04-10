package object libt {

  type Path = List[PathPart]

  implicit def symbol2Route(s: Symbol)  = Route(s)
  implicit def int2Index(i: Int) = Index(i)
  
  object Path {
    def apply(parts : PathPart*) : Path = List(parts : _*)
  }
}