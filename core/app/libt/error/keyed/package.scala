package libt.error

package object keyed {

  type KeyedMessage[M] = (M, Seq[String])

  type Validated[M, A] = generic.Validated[KeyedMessage[M], Seq[A]]
  type Invalid[M] = generic.Invalid[KeyedMessage[M]]
  type Doubtful[M, +A] = generic.Doubtful[KeyedMessage[M], A]

  val Doubtful = generic.Doubtful
  val Valid = generic.Valid
  val Invalid = generic.Invalid

  object Validated {

    def flatConcat[M, A](results: Seq[(M, generic.Validated[String, Seq[A]])]): Validated[M, A] = {
      results concatMap {
        case (key, result) => result match {
          case v@Valid(_) => v
          case Doubtful(v, w@_*) => Doubtful(v, (key, w))
          case i => Invalid((key, i.messages))
        }
      } map(_.flatten)
    }
  }
}
