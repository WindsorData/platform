package controllers.generic

import com.mongodb.casbah.MongoDB

import java.io.ByteArrayOutputStream
import output.{OutputWriter, StandardWriter}

import play.api.mvc.Result
import play.api.mvc.Controller

import persistence._

import libt._

trait SpreadsheetDownloader { self: Controller =>

  def createSpreadsheetResult(writer: OutputWriter, names: Seq[String], range: Int)(db: CompaniesDb): Option[Result] = {
    db.findCompaniesBy(names) match {
      case Nil => None
      case companies: Seq[Model] =>
        Some(Ok(writeToByteArray(writer, companies, range)).withHeaders(
          CONTENT_TYPE -> "application/octet-stream",
          CONTENT_DISPOSITION -> "attachment; filename=company.xls"))
    }
  }

  protected def writeToByteArray(writer: OutputWriter, models: Seq[Model], range:Int) = {
    val out = new ByteArrayOutputStream()
    writer.write(out, models, range)
    out.toByteArray
  }
}
