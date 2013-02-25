package windsor.input
import org.scalatest.path.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.FileInputStream

@RunWith(classOf[JUnitRunner])
class SpreadsheetLoaderSpec extends FunSpec {
  describe("An importer") {

    it("should import Companies") {
      assert(loadSpreadsheet("Sample1.xlsx") ===
        Seq(Company("X", 1.0, 2010), 
            Company("Z", 2.0, 2010), 
            Company("K", 4.0, 2012), 
            Company("M", 8.0, 2013)))
    }

    it("should support empty inputs") {
      assert(loadSpreadsheet("Empty.xlsx") === Seq())
    }

    it("should reject bad inputs") {
      assert(loadSpreadsheet("Bad.xlsx") === Seq())
    }

  }
  
  def loadSpreadsheet(name: String) = {
    import Closeables._
    new FileInputStream("src/test/resources/"+ name).processWith {
      x => SpreadsheetLoader.load(x)
    }
  }
}
