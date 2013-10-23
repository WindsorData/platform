package libt

import org.scalatest.FunSpec

class PrototypeModelSpec extends FunSpec {

  describe("tmodel") {
    describe("can instantiate") {
      it("empty prototype models") {
        TModel().exampleWith()
      }

      it("empty prototype models with some custom elements") {
        val protoModel = TModel('foo -> TString,'bar -> TInt).exampleWith('foo -> Value("something"))
        assert(protoModel === Model('foo -> Value("something"), 'bar -> Value()))
      }

      it("empty prototype models with all elements customized") {
        val protoModel = TModel('foo -> TString,'bar -> TInt)
          .exampleWith('foo -> Value("something"), 'bar -> Value(1))
        assert(protoModel === Model('foo -> Value("something"), 'bar -> Value(1)))
      }
    }
  }
}
