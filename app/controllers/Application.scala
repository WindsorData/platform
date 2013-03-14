package controllers

import play.api._
import play.api.mvc._
import input.SpreadsheetLoader
import java.io.InputStream
import util.Closeables
import util.FileManager
import java.io.FileInputStream
import com.mongodb.casbah.MongoClient
import input.SpreadsheetWriter
import play.api.data._
import play.api.data.Forms._
import java.io.ByteArrayOutputStream
import model.CompanyFiscalYear
import com.mongodb.DBObject

//No content-negotiation yet. Just assume HTML for now
object Application extends Controller {

  import persistence._
  import util.persistence._

  implicit val companiesCollection = MongoClient()("windsor")("companies")

  def index = Action {
    Ok(views.html.index())
  }

  def newCompany = Action(parse.multipartFormData) { request =>
    request.body.file("dataset").map { dataset =>
      val executives = FileManager.loadSpreadsheet(dataset.ref.file.getAbsolutePath)
      executives.foreach(_.save())
      Ok("File uploaded successfully")
    }.getOrElse {
      Redirect(routes.Application.index).flashing("error" -> "Missing file")
    }
  }

  def company = Action { request =>
    //TODO: need to change this by using play.api.data.Forms
    val name = request.body.asFormUrlEncoded.get("company-name")(0)
    val year = request.body.asFormUrlEncoded.get("year")(0)

    //  	Ok(findCompanyBy(name, year.toInt).toString)
    val out = new ByteArrayOutputStream()
    try {
      SpreadsheetWriter.write(out, findCompanyBy(name, year.toInt))
      Ok(out.toByteArray()).withHeaders(CONTENT_TYPE -> "application/octet-stream",
    		  							CONTENT_DISPOSITION -> "attachment; filename=company.xls")
    } catch {
      case e: RuntimeException => Ok("No Results")
    }
  }

  def companies = Action {
    Ok(views.html.companies())
  }

}