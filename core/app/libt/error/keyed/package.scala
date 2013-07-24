package libt.error

package object keyed {

  type KeyedMessage = (String, String, Seq[String])

  type Validated[A] = generic.Validated[KeyedMessage, Seq[A]]
  type Invalid = generic.Invalid[KeyedMessage]
  type Doubtful[+A] = generic.Doubtful[KeyedMessage, A]

  val Doubtful = generic.Doubtful
  val Valid = generic.Valid
  val Invalid = generic.Invalid

  object Validated {

    def flatConcat[A](results: Seq[(String, String, generic.Validated[String, Seq[A]])]): Validated[A] = {
      results concatMap {
        case (key, cusip, result) => result match {
          case v@Valid(_) => v
          case Doubtful(v, w@_*) => Doubtful(v, (key, cusip, w))
          case i => Invalid((key, cusip, i.messages))
        }
      } map(_.flatten)
    }
  }
}
