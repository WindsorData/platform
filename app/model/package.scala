package object model {

  implicit def option2Input[A](option: Option[A]) =
    Input(option, None, None, None, None)

}