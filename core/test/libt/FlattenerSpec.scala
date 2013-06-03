package libt

import org.scalatest.FlatSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import libt.error._
import libt._

@RunWith(classOf[JUnitRunner])
class FlattenerSpec extends FlatSpec {

  behavior of "Model flattener"

  it should "be able to flat a single model" in {
    val model = Model(
      'key -> Value("hello"),
      'value -> Col(
        Model('value -> Value("world")),
        Model('value -> Value("!"))))

    val flattenedModel = model.flattenWith(PK(Path('key)), Path('value))

    assert(flattenedModel === Seq(
      Model(
        'key -> Value("hello"),
        'value -> Value("world")),
      Model(
        'key -> Value("hello"),
        'value -> Value("!"))))
  }

  it should "have root pks in flattened models" in {
    val model = Model(
      'key1 -> Value("Hey"),
      'key2 -> Value("!"),
      'key3 -> Value("hello"),
      'value -> Col(
        Model('value -> Value("world")),
        Model('value -> Value("!"))))

    val flattenedModel = model.flattenWith(PK(Path('key1), Path('key2), Path('key3)), Path('value))

    assert(flattenedModel === Seq(
      Model(
        'key1 -> Value("Hey"),
        'key2 -> Value("!"),
        'key3 -> Value("hello"),
        'value -> Value("world")),
      Model(
        'key1 -> Value("Hey"),
        'key2 -> Value("!"),
        'key3 -> Value("hello"),
        'value -> Value("!"))))
  }

  it should "work with multiple roots" in {
    val s: Seq[Validated[Model]] = Seq()
    val models = Seq(
      Model(
        'key -> Value("hello"),
        'value -> Col(
          Model('value -> Value("world")),
          Model('value -> Value("!")))),

      Model(
        'key -> Value("good"),
        'value -> Col(
          Model('value -> Value("bye")),
          Model('value -> Value("day")))))

    val flattenedModels = Model.flattenWith(models, PK(Path('key)), Path('value))

    assert(flattenedModels === Seq(
      Model(
        'key -> Value("hello"),
        'value -> Value("world")),
      Model(
        'key -> Value("hello"),
        'value -> Value("!")),
      Model(
        'key -> Value("good"),
        'value -> Value("bye")),
      Model(
        'key -> Value("good"),
        'value -> Value("day"))))
  }

}