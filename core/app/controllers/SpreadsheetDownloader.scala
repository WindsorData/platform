package controllers

import play.api.mvc.Result
import play.api.mvc.Controller
import java.io.ByteArrayOutputStream
import persistence._
import output.SpreadsheetWriter
import com.mongodb.casbah.MongoDB

trait SpreadsheetDownloader { self: Controller =>

  def createSpreedsheet(names: Seq[String], range: Int)(implicit db: MongoDB) : Option[Result] = {
    val out = new ByteArrayOutputStream()
    findCompaniesBy(names, range).map(
      founded => {
        SpreadsheetWriter.write(out, founded, range)
        Ok(out.toByteArray()).withHeaders(CONTENT_TYPE -> "application/octet-stream",
          CONTENT_DISPOSITION -> "attachment; filename=company.xls")
      }
    )
  }
}
