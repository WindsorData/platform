package libt.error

import org.scalatest.FunSpec
import libt.error.generic.{Invalid, Doubtful, Valid}

class ValidatedSpec extends FunSpec {

  describe("map") {
    it("should answer valid for valid") {
      assert(Valid(2).map(_ + 1) === Valid(3))
    }
    it("should answer invalid for invalid") {
      assert(Invalid("foo", "bar").map((_: Int) + 1) === Invalid("foo", "bar"))
    }
  }

  describe("flatMap") {
    it("should answer valid when valid combined with valid") {
      assert(Valid(2).flatMap(x => Valid(x + 1)) === Valid(3))
    }

    it("should answer invalid when invalid combined with invalid") {
      assert(Invalid("foo", "bar").flatMap((x: Int) => Valid(x + 1)) === Invalid("foo", "bar"))
    }

    it("should answer invalid when valid combined with invalid") {
      assert(Valid(3).flatMap(x => Invalid("foobar")) === Invalid("foobar"))
    }

    it("should answer invalid when invalid combined with valid") {
      assert(Invalid("foo", "bar").flatMap(x => Invalid("foobar")) === Invalid("foo", "bar"))
    }
  }

  describe("for comprehensions") {
    it("should compute all expressions if all are valid") {
      val k = for (x <- Valid(2); y <- Valid(3); z <- Valid(5))
      yield x + y + z
      assert(k === Valid(10))
    }
  }

  describe("concat") {
    it("should be invallid when any of the concated elements are invallid") {
      assert(Validated.concat(Seq(Valid(2), Invalid("foo"), Valid(4))) === Invalid("foo"))
    }

    it("should concat all the errors when there is more than one invalid") {
      assert(Validated.concat(Seq(Valid(2), Invalid("foo"), Invalid("bar"))) === Invalid("foo", "bar"))
    }

    it("should construct a list with all the values when all are valid") {
      assert(Validated.concat(Seq(Valid(2), Valid(3), Valid(4))) === Valid(Seq(2, 3, 4)))
    }
  }

  describe("andThen") {
    it("should add error messages when both are invalid") {
      assert( (Invalid("foo") andThen Invalid("bar"))  === Invalid("foo", "bar"))
    }
    it("should be invalid when first is invalid") {
      assert( (Invalid("foo") andThen Valid(4))  === Invalid("foo"))
    }
    it("should be invalid when second is invalid") {
      assert( (Valid(1) andThen Invalid("bar"))  === Invalid("bar"))
    }
    it("should be valid when both are valid") {
      assert( (Valid(1) andThen Valid(3))  === Valid(3))
    }

    it("should be doubtful value when first is doubtful") {
      assert((Doubtful(1, "mmm") andThen Valid(2)) === Doubtful(2, "mmm"))
    }

    it("should be doubtful with merged messages when both are doubtful") {
      assert((Doubtful(1, "mmm") andThen Doubtful(2, "aaa")) === Doubtful(2, "mmm", "aaa"))
    }

    it("should be Invalid when first is invalid and second is doubtful") {
      assert((Invalid("mmm") andThen Doubtful(2, "aaa")) === Invalid("mmm", "aaa"))
    }
  }
}