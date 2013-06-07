package libt.error

package object keyed {

  type KeyedMessage = (String, Seq[String])
  type Validated[A] = generic.Validated[KeyedMessage, Seq[A]]
  
  def flatJoin[A](results: Seq[(String, Seq[generic.Validated[String, A]])]): Validated[A] = {
    Validated.flatJoin(results.map {
      case (key, values) => Validated.join(values) match {
        case v @ Valid(_) => v
        case i => Invalid(key -> i.toErrorSeq)
      }
    })
  }

}