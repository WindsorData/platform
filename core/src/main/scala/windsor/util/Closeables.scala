package windsor.input
import java.io.Closeable

object Closeables {

  implicit def closeable2RichCloseable[A <: Closeable](closeable: A) =
    new {
      def processWith[B](op: A => B) = try { op(closeable) } finally { closeable.close() }
    }

}