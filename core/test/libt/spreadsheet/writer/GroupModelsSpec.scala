package libt.spreadsheet.writer

import org.scalatest._
import output.FullTop5Writer._
import libt._


class GroupModelsSpec extends FunSpec with BeforeAndAfter {

  describe("group and order models") {
    val models = Seq(
      Model('disclosureFiscalYear -> Value(2010), 'cusip -> Value("aaaa")),
      Model('disclosureFiscalYear -> Value(2010), 'cusip -> Value("cccc")),
      Model('disclosureFiscalYear -> Value(2011), 'cusip -> Value("aaaa")),
      Model('disclosureFiscalYear -> Value(2012), 'cusip -> Value("aaaa")),
      Model('disclosureFiscalYear -> Value(2010), 'cusip -> Value("bbbb")),
      Model('disclosureFiscalYear -> Value(2012), 'cusip -> Value("bbbb"))
    )

    it("should group by cusip") {
      val groupedModels = ModelGrouper(models)
      assert(groupedModels.size === 3)
      assert(groupedModels.exists(_._1.equals("aaaa")))
      assert(groupedModels.exists(_._1.equals("bbbb")))
      assert(groupedModels.exists(_._1.equals("cccc")))
    }

    it("should sorted by cusip") {
      val groupedModels = ModelGrouper(models)
      assert( Seq(
        "aaaa" -> Seq(2012, 2011, 2010),
        "bbbb" -> Seq(2012, 2010),
        "cccc" -> Seq(2010)
      ).forall { case (cusip, years) =>
        groupedModels.find(_._1 == cusip).get._2.map(_ /#/ 'disclosureFiscalYear).equals(years)
      })
    }
  }

}
