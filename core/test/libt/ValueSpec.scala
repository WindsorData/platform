package libt

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import libt._
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec

@RunWith(classOf[JUnitRunner])
class ValueSpec extends FlatSpec {

  it should "map" in {
    assert(Value(4).map(_ + 1) === Value(5))
  }
  it should "implement orDefault" in {
    assert(Value(4).orDefault(5) === Value(4))
    assert(Value().orDefault(5) === Value(5))
  }
  it should "answer metadata, ordered" in {
    assert(Value(4).metadataSeq === Seq(None, None, None, None))
    assert(
      Value(Some(4), Some("calc"), Some("comment"), Some("note"), Some("link")).metadataSeq
        === Seq(Some("calc"), Some("comment"), Some("note"), Some("link")))
  }

}