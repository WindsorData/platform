package util

object IndexedTraversables {
  implicit def traversable2WithIndex[A](traversable: Traversable[A]) = new {
    def foreachWithIndex(f: (A, Int) => Unit) {
      var index = 0
      traversable.foreach { x =>
        f(x, index)
        index += 1
      }
    }
  }
}