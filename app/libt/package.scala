package object libt {

  /**
   * A path is a pointer to an element inside another element.
   */
  type Path = List[PathPart]

  /* Small DSL for declaring paths  */

  implicit def symbol2Route(s: Symbol) = Route(s)
  
  implicit def int2Index(i: Int) = Index(i)

  implicit def path2RichPath(path: Path) = new {
    def titles = path.map(_.name)
  }
  
  object Path {
    def apply(parts: PathPart*): Path = List(parts: _*)
    
  }

  /**
   * A PK is just a sequence of Paths that represent
   * the primary identifier an element
   */
  //TODO rename
  type PK = Seq[Path]
  
  object PK {
    def apply(elements: Path*) : PK = elements
  }
}