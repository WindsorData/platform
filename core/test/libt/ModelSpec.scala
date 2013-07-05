package libt

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec

class ModelSpec extends FunSpec {

  describe("inersect") {
    it("should return an empty model for an empty PK") {
      assert((Model('f -> Value("f"), 'g -> Value("g")) intersect Seq()) === Model())
    }

    it("should return a one element model for a single element, valid pk") {
      assert((Model('f -> Value("f"), 'g -> Value("g")) intersect Seq(Path('f))) === Model('f -> Value("f")))
    }

    it("should use the last part of the path as key when using non trivial paths") {
      assert((Model('f -> Model('g -> Value("g"))) intersect Seq(Path('f, 'g))) === Model('g -> Value("g")))
    }
  }

  describe("get") {
    it("should return none for missing keys") {
      assert(Model().get('a) === None)
    }
  }

  describe("flatten") {
    it("should be able to flat a single model") {
      val model = Model(
        'key -> Value("hello"),
        'value -> Col(
          Model('value -> Value("world")),
          Model('value -> Value("!"))))

      val flattenedModel = model.flattenWith(Seq(Path('key)), Path('value, *))

      assert(flattenedModel === Seq(
        Model(
          'key -> Value("hello"),
          'value -> Value("world")),
        Model(
          'key -> Value("hello"),
          'value -> Value("!"))))
    }

    it("have root pks in flattened models") {
      val model = Model(
        'key1 -> Value("Hey"),
        'key2 -> Value("!"),
        'key3 -> Value("hello"),
        'value -> Col(
          Model('value -> Value("world")),
          Model('value -> Value("!"))))

      val flattenedModel = model.flattenWith(Seq(Path('key1), Path('key2), Path('key3)), Path('value, *))

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

    it("work with multiple roots") {
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

      val flattenedModels = Model.flattenWith(models, Seq(Path('key)), Path('value, *))

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

}