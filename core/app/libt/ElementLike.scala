package libt
import libt.util._

/**
 * Mixin that provides rescursive structure trasversing code and casting code for
 * hierarchies that implement Model-Value-Col Algebraic Data Types 
 * 
 * @param ElementType the type of the actual class that is including this mixin
 *  
 * @author flbulgarelli
 */
trait ElementLike[ElementType] { self : ElementType =>
  type ModelType <: ModelLike[ElementType]
  type ColType <: ColLike[ElementType]
  type ValueType[A] <: ValueLike[ElementType, A]
  
  /*===Casting===*/

  /**Casts this element to a Col*/
  def asCol: ColType = asInstanceOf[ColType]
  
  /**Casts this element to a Model*/
  def asModel: ModelType = asInstanceOf[ModelType]
  
  /**Casts this element to a Value*/
  def asValue[A]: ValueType[A] = asInstanceOf[ValueType[A]]
  
  /*===Traversing===*/

  /**
   * Answers the ElementLike element at the given path.
   * Fails if the path points to something else
   */
  def apply(path: Path): ElementType = umatch((path, this)) {
    case (Nil, _) => this
    case (Index(index) :: tail, self: ColLike[ElementType]) => self(index)(tail)
    case (* :: tail, self: ColLike[ElementType]) => self(0)(tail)  
    case (Route(field) :: tail, self: ModelLike[ElementType]) => self(field)(tail)
  }
}
trait ModelLike[ElementType] {
  /**Answers the element at the given key*/
  def apply(key: Symbol): ElementLike[ElementType]
}
trait ColLike[ElementType] {
  /**Answers the element at the given index*/
  def apply(index: Int): ElementLike[ElementType]
}
trait ValueLike[ElementType, A]







