package util

object ErrorHandler {
  
  def handle[A](action: => A): Either[String, A] = {
    try {
      Right(action)
    } catch {
      case e: RuntimeException =>
        Left(e.getMessage())
    }
  }
  
  implicit def results2RichResults[A,B](results: Seq[Either[A, B]]) = new {
    def hasErrors = results.exists(_.isLeft)
    def errors = results.filter(_.isLeft)
  }
}