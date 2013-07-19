package controllers.generic

import com.mongodb.casbah.MongoDB

import java.io.ByteArrayOutputStream
import output.SpreadsheetWriter

import play.api.mvc.Result
import play.api.mvc.Controller

import persistence.CompaniesDb._

import libt._

trait SpreadsheetDownloader { self: Controller =>

  def createSpreadsheetResult(names: Seq[String], range: Int)(implicit db: MongoDB): Option[Result] = {
    findCompaniesBy(names) match {
      case Nil => None
      case companies: Seq[String] =>
        Some(Ok(writeToByteArray(companies, range)).withHeaders(
          CONTENT_TYPE -> "application/octet-stream",
          CONTENT_DISPOSITION -> "attachment; filename=company.xls"))
    }
  }

  protected def writeToByteArray(models: Seq[Model], range:Int) = {
    val out = new ByteArrayOutputStream()
    SpreadsheetWriter.write(out, models, range)
    out.toByteArray
  }
}
