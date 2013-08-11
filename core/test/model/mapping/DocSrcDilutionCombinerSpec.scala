package model.mapping

import org.scalatest.{FlatSpec, FunSpec}
import model.mapping.dilution.DilutionDocSrcCombiner
import libt._
import libt.error.generic._


class DocSrcDilutionCombinerSpec extends FlatSpec {

  val inputModels = Seq(
    Seq(Model('x -> Value(1))),
    Seq(Model('y -> Value(2))),
    Seq(Model('z -> Value(3))))

  def Year(value:Int)  = Valid(Model('disclosureFiscalYear -> Value(value)))

  it should "combine company db into a single model for same year" in new DilutionDocSrcCombiner {
    assert(combineModels(
      Seq(
        (Year(2010), 'usageAndSVTData, _.head),
        (Year(2010), 'bsInput, _.head),
        (Year(2010), 'dilution, _.head)
      ),
      inputModels,
      Model('ticker -> Value("foo")))
      ===
      Valid(Seq(
        Model(
          'disclosureFiscalYear -> Value(2010),
          'ticker -> Value("foo"),
          'companyDB -> Model(
            'usageAndSVTData -> Model('x -> Value(1)),
            'bsInput -> Model('y -> Value(2)),
            'dilution -> Model('z -> Value(3)))
        )
      ))
    )
  }

  it should "not combine company db for diferent years" in new DilutionDocSrcCombiner {
    assert(combineModels(
      Seq(
        (Year(2010), 'usageAndSVTData, _.head),
        (Year(2011), 'bsInput, _.head),
        (Year(2010), 'dilution, _.head)
      ),
      inputModels,
      Model('ticker -> Value("foo")))
      ===
      Valid(Seq(
        Model(
          'disclosureFiscalYear -> Value(2011),
          'ticker -> Value("foo"),
          'companyDB -> Model(
            'bsInput -> Model('y -> Value(2))
          )
        ),
        Model(
          'disclosureFiscalYear -> Value(2010),
          'ticker -> Value("foo"),
          'companyDB -> Model(
            'usageAndSVTData -> Model('x -> Value(1)),
            'dilution -> Model('z -> Value(3)))
        )
      ))
    )
  }

}
