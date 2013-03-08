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
import persistence._
import model._

//No content-negotiation yet. Just assume HTML for now
object Application extends Controller {

  import persistence._
  import util.persistence._

  implicit val companiesCollection = MongoClient()("windsor")("companies")

  val companyForm = Form(
    tuple(
      "Company Name" -> nonEmptyText,
      "Company Fiscal Year" -> nonEmptyText))

  def index = Action {
    Redirect(routes.Application.companies)
  }

  def newCompany = Action(parse.multipartFormData) { request =>
    request.body.file("dataset").map { dataset =>
      val companies = FileManager.loadSpreadsheet(dataset.ref.file.getAbsolutePath)
      companies.foreach { c =>
        findCompanyBy(c.name.value.get, c.disclosureFiscalYear.value.get).getOrElse(c).update(c)
      }
      Ok("File uploaded successfully")
    }.getOrElse {
      Redirect(routes.Application.companies).flashing("error" -> "Missing file")
    }
  }

  def reports = Action {
    Ok(views.html.reports())
  }

  def searchCompany = Action {
    Ok(views.html.searchCompanies(companyForm))
  }

  def doSearch = Action { implicit request =>

    companyForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.searchCompanies(formWithErrors)),
      values => {
        val name = values._1
        val year = values._2
        val out = new ByteArrayOutputStream()
        findCompanyBy(name, year.toInt) match {
          case Some(founded) => {
            SpreadsheetWriter.write(out, founded)
            Ok(out.toByteArray()).withHeaders(CONTENT_TYPE -> "application/octet-stream",
              CONTENT_DISPOSITION -> "attachment; filename=company.xls")
          }
          case None => Ok("No Results")
        }
      })
  }

  def companies = Action {
    Ok(views.html.companies(CompanyFiscalYear.all()))
  }

}