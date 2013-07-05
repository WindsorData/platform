package libt

import org.scalatest.FlatSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

class ValidationSpec extends FlatSpec {

  behavior of "TElement valdations"

  it should "pass on empty models" in {
    TModel().validate(Model())
  }

  it should "pass on valid non empty models" in {
    TModel('foo -> TString).validate(Model('foo -> Value("hello")))
  }

  ignore should "fail for illtyped values" in {
    TModel('foo -> TString).validate(Model('foo -> Value(10)))
  }

  ignore should "fail for illegal structures models" in {
    TModel('foo -> TString).validate(Model('foo -> Value("")))
  }

  it should "pass for valid enums" in {
    TModel('foo -> TStringEnum("foo", "bar")).validate(Model('foo -> Value("foo")))
  }

  it should "pass for empty enums" in {
    TModel('foo -> TStringEnum("foo", "bar")).validate(Model('foo -> Value()))
  }

  it should "fail for invalid enum values" in {
    intercept[Throwable] {
      TModel('foo -> TStringEnum("foo", "bar")).validate(Model('foo -> Value("foobar")))
    }
  }

  it should "fail for invalid enums in cols" in {
    intercept[Throwable] {
      TModel('foo -> TCol(TStringEnum("foo", "bar"))).validate(Model('foo -> Col(Value("baz"))))
    }
  }

  it should "pass for valid enum values in cols" in {
    TModel('foo -> TCol(TStringEnum("foo", "bar"))).validate(Model('foo -> Col(Value("foo"), Value("bar"))))
  }
}