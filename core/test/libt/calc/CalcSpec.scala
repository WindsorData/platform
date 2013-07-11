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
      assert(Value(1: BigDecimal).isConsistent)
    }

    it("should be consistent when calc is none") {
      assert(Value[BigDecimal](None, Some("1+2"), None, None, None).isConsistent)
    }

    it("should be consistent if values match") {
      assert(Value(Some(3: BigDecimal), Some("1+2"), None, None, None).isConsistent)
      assert(!Value(Some(1: BigDecimal), Some("1+2"), None, None, None).isConsistent)
      assert(Value(Some(0.71: BigDecimal), Some("=341.885/484"), None, None, None).isConsistent)
      assert(Value(Some(0.5: BigDecimal), Some("=341.885/685"), None, None, None).isConsistent)
    }
  }


}
