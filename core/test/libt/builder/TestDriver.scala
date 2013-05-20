package libt.builder

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import java.util.Stack
import libt.Value
import libt._

@RunWith(classOf[JUnitRunner])
class TestDriver extends FlatSpec {

  behavior of "model builder"

  it should "create empty models" in {
    assert(new ModelBuilder().build === Model())
  }

  it should "create models with a single field" in {
    val builder = new ModelBuilder()
    builder += (Path('foo) -> Value("foo"))
    assert(builder.build === Model('foo -> Value("foo")))
  }

  it should "create models with several fields" in {
    val builder = new ModelBuilder()
    builder += (Path('foo) -> Value("foo"))
    builder += (Path('bar) -> Value("bar"))
    assert(builder.build === Model(
      'foo -> Value("foo"),
      'bar -> Value("bar")))
  }

  it should "create models with nested fields" in {
    val builder = new ModelBuilder()
    builder += (Path('foo, 'bar) -> Value("foo"))
    assert(builder.build === Model(
      'foo ->
        Model('bar -> Value("foo"))))
  }

  ignore should "create models with collection of values" in {
    fail()
  }

  it should "create models with collections of models" in {
    val builder = new ModelBuilder()
    builder += (Path('foo, 0, 'bar) -> Value("bar1"))
    builder += (Path('foo, 0, 'barr) -> Value("bar2"))
    builder += (Path('foo, 1, 'bar) -> Value("bar3"))
    builder += (Path('foo, 1, 'barr) -> Value("bar4"))

    assert(builder.build ===
      Model(
        'foo -> Col(
          Model('bar -> Value("bar1"),
            'barr -> Value("bar2")),
          Model('bar -> Value("bar3"),
            'barr -> Value("bar4")))))
  }

}
