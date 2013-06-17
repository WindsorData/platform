package libt.builder
import libt._
import scala.collection.mutable.{ Map => MutableMap }
import scala.collection.mutable.Buffer
import scala.annotation.tailrec

/***
 * A mutable object for building immutable Models, by inserting values at given Paths
 * @author flbulgarelli
 * @author mcorbanini
 * */
class ModelBuilder {
  /**The mutable representation of Model*/
  private type MModel = MutableMap[Symbol, AnyRef]
  private def MModel() = MutableMap[Symbol, AnyRef]()
  
  /**The mutable representation of Col*/
  private type MCol = Buffer[AnyRef]

  /**the mutable representation of the model being built*/
  private val model = MutableMap[Symbol, AnyRef]()

  /**
   * Adds a value at the given path in the model under construction
   * */
  def +=(modelEntry: (Path, Value[_])) = setWithRoot(model, modelEntry._1, modelEntry._2)

  @tailrec private def setWithRoot(untypedRoot: AnyRef, path: Path, value: Value[_]): Unit = {
    val root = untypedRoot.asInstanceOf[MModel]
    path match {
      //Set value at root's field
      case Route(field) :: Nil => root += (field -> value)
      //Set value at submodel
      case Route(field) :: Route(next) :: tail =>
        setWithRoot(root.getOrElseUpdate(field, MModel()), Route(next) :: tail, value)
      //set value at subcollection element   
      case Route(field) :: Index(index) :: Nil =>
        root.getOrElseUpdate(field, MModel()).asInstanceOf[MCol](0) = value //FIXME
      //set value at subcollection's model
      case Route(field) :: Index(index) :: tail =>
        setWithRoot(
          getOrAddNew(root.getOrElseUpdate(field, Buffer(MModel())).asInstanceOf[MCol], index), tail, value)
    }
  }
  
  private def getOrAddNew(buf: MCol, index: Int): AnyRef = {
    if (buf.size - 1 < index) {
      buf.insert(index, MModel())
    }
    buf(index)
  }

  def build: Model = convert(model).asModel

  private def convert(element: AnyRef): Element = element match {
    case v: Value[_] => v
    case m: MModel => Model(m.mapValues(convert(_).asInstanceOf[Element]).toSet)
    case c: MCol => Col(c.map(convert): _*)
  }
}