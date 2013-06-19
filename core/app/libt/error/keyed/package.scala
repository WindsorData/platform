package libt.error

package object keyed {

  type KeyedMessage = (String, Seq[String])
  type Validated[A] = generic.Validated[KeyedMessage, Seq[A]]
  
  //TODO remove
  def flatJoin[A](results: Seq[(String, Seq[generic.Validated[String, A]])]): Validated[A] = {
    Validated.flatJoin(results.map {
      case (key, values) => Validated.concat(values) match {
        case v @ Valid(_) => v
        case Doubtful(v, w) => Doubtful(v, key -> Seq(w))
        case i => Invalid(key -> i.messages)
      }
    })
  }

  def join[A](results: Seq[(String, generic.Validated[String, A])]): Validated[A] = {
    Validated.concat(results.map {
      case (key, value) => value match {
        case v @ Valid(_) => v
        case i => Invalid(key -> i.messages)
      }
    })
  }
}
