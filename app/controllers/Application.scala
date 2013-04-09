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
import views.html.defaultpages.badRequest

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
      try {
        val companies = FileManager.loadSpreadsheet(dataset.ref.file.getAbsolutePath)
        companies.foreach(_.update)
        Ok(views.html.companyUploadSuccess())
      } catch {
        case e: RuntimeException => {
          Logger.error(e.getMessage)
          BadRequest(e.getMessage())
        }
      }
    }.getOrElse {
      Redirect(routes.Application.companies).flashing("error" -> "Missing file")
    }
  }

  def reports = Action {
    Ok(views.html.reports())
  }

  def searchCompany = Action {
    Ok(views.html.searchCompanies(companyForm,
      CompanyFiscalYear.getAllNames,
      CompanyFiscalYear.getAllFiscalYears.map(_.toString)))
  }

  def doSearch = Action { implicit request =>

    companyForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.searchCompanies(formWithErrors,
          CompanyFiscalYear.getAllNames,
          CompanyFiscalYear.getAllFiscalYears.map(_.toString))),
      values => {
        val name = values._1
        val year = values._2
        val out = new ByteArrayOutputStream()
        findCompanyBy(name, year.toInt) match {
          case Some(founded) => {
            SpreadsheetWriter.write(out, Seq(founded))
            Ok(out.toByteArray()).withHeaders(CONTENT_TYPE -> "application/octet-stream",
              CONTENT_DISPOSITION -> "attachment; filename=company.xls")
          }
          case None => Ok(views.html.searchWithoutResults())
        }
      })
  }

  def companies = Action {
    Ok(views.html.companies(CompanyFiscalYear.all()))
  }

}