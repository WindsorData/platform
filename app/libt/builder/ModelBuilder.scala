package libt.builder
import libt._
import scala.collection.mutable.{ Map => MutableMap }
import scala.collection.mutable.Buffer

class ModelBuilder {
  type MModel = MutableMap[Symbol, AnyRef]
  type MCol = Buffer[AnyRef]
  val model = MutableMap[Symbol, AnyRef]()

  def +=(modelEntry: (Path, Value[_])) = setWithRoot(model, modelEntry._1, modelEntry._2)

  private def setWithRoot(untypedRoot: AnyRef, path: Path, value: Value[_]): Unit = {
    val root = untypedRoot.asInstanceOf[MModel]
    def getOrCreate(m: MModel, k: Symbol, default: AnyRef = MutableMap[Symbol, AnyRef]()) =
      m.getOrElseUpdate(k, default)

    def getOrAddNew(buf: MCol, index: Int): AnyRef = {
      if (buf.size - 1 < index) {
        buf.insert(index, MutableMap[Symbol, AnyRef]())
      }
      buf(index)
    }

    path match {
      case Route(field) :: Nil => root += (field -> value)
      case Route(field) :: Route(next) :: tail =>
        setWithRoot(getOrCreate(root, field), Route(next) :: tail, value)
      case Route(field) :: Index(index) :: Nil =>
        getOrCreate(root, field).asInstanceOf[MCol](0) = value
      case Route(field) :: Index(index) :: tail =>
        setWithRoot(
          getOrAddNew(getOrCreate(root, field, Buffer(MutableMap[Symbol, AnyRef]())).asInstanceOf[MCol], index),
          tail, value)
    }
  }

  def build: Model = convert(model).asInstanceOf[Model]

  def convert(element: AnyRef): Element = element match {
    case v: Value[_] => v
    case m: MModel => Model(m.mapValues(convert(_).asInstanceOf[Element]).toSet)
    case c: MCol => Col(c.map(convert): _*)
  }
}