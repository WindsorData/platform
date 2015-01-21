package libt.util

object Lists {
  implicit def traversable2WithIndex[A](traversable: Traversable[A]) = new {
    def foreachWithIndex(f: (A, Int) => Unit) {
      var index = 0
      traversable.foreach { x =>
        f(x, index)
        index += 1
      }
    }
  }

  def gzip[A](xs: Seq[Seq[A]]): Seq[Seq[A]] = xs match {
    case m1 :: m2 :: Nil => m1.zip(m2).map {
      case (x, y) => Seq(x, y)
    }
    case head :: tail => head.zip(gzip(tail)).map {
      case (x, xs) => x +: xs
    }
  }
}