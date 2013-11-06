package libt

import org.scalatest.FunSpec
import org.joda.time.DateTime

class PropertyMissingElementSpec extends FunSpec {

  describe("Property missing on Model") {
    it("should calculate missing property") {
     assert(Model('disclosureFiscalYear -> Value(2013)).get('disclosureFiscalYearDate) ===
       Some(Value(new DateTime().withDayOfMonth(1).withMonthOfYear(1).withYear(2013).toDate)))
    }
  }

}
