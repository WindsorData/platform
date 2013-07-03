package libt.calc

import libt._
import org.scalatest.FunSpec

class CalcSpec extends FunSpec {
  describe("fomula") {
    it("should  drop initial equals sign") {
      assert(Calc("=4").formula === "4")
    }

    it("should do trim left") {
      assert(Calc(" 4").formula === "4")
    }

    it("should drop initial equals sign even when there are whitespaces") {
      assert(Calc(" = 4").formula === "4")
    }
  }

  describe("eval") {
    it("should eval expressions") {
      assert(Calc(" 4 + 5")() === 9)
    }
  }

  describe("isConsistent") {
    it("should be consistent when value is none") {
      assert(Value(Some(1), None, None, None, None).isConsistent)
    }

    it("should be consistent when calc is none") {
      assert(Value(None, Some("1+2"), None, None, None).isConsistent)
    }

    it("should be consisten iff values match") {
      assert(Value(Some(3), Some("1+2"), None, None, None).isConsistent)
      assert(!Value(Some(1), Some("1+2"), None, None, None).isConsistent)
    }
  }


}
