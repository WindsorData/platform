package libt

import org.scalatest.FunSpec

class ExampleModelSpec extends FunSpec {

  describe("tmodel") {
    describe("can instantiate") {
      it("empty prototype models") {
        TModel().exampleWith()
      }

      it("empty prototype models with some custom elements") {
        val exampleModel = TModel('foo -> TString,'bar -> TInt).exampleWith('foo -> Value("something"))
        assert(exampleModel === Model('foo -> Value("something"), 'bar -> Value()))
      }

      it("empty prototype models with all elements customized") {
        val exampleModel = TModel('foo -> TString,'bar -> TInt)
          .exampleWith('foo -> Value("something"), 'bar -> Value(1))
        assert(exampleModel === Model('foo -> Value("something"), 'bar -> Value(1)))
      }
    }
  }
}
