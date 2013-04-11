package object libt {

  /**
   * A path is a pointer to an element inside another element. 
   **/
  type Path = List[PathPart]
  
  /* Small DSL for declaring paths  */

  implicit def symbol2Route(s: Symbol)  = Route(s)
  implicit def int2Index(i: Int) = Index(i)
  object Path {
    def apply(parts : PathPart*) : Path = List(parts : _*)
  }
}