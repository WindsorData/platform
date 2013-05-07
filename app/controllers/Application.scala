package controllers

import play.api._
import play.api.mvc._
import java.io.InputStream
import util.Closeables
import util.FileManager._
import java.io.FileInputStream
import com.mongodb.casbah.MongoClient
import output.SpreadsheetWriter
import play.api.data._
import play.api.data.Forms._
import java.io.ByteArrayOutputStream
import com.mongodb.DBObject
import persistence._
import model._
import views.html.defaultpages.badRequest
import libt.Model
import model.mapping._
import output.SpreadsheetWriter

//No content-negotiation yet. Just assume HTML for now
object Application extends Controller {

  import persistence._

  implicit val db = MongoClient()("windsor")

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
        val companies = CompanyFiscalYearReader.read(dataset.ref.file.getAbsolutePath)
        companies.foreach(updateCompany(_))
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
      allCompanies :: findAllCompaniesNames.toList,
      allYears :: findAllCompaniesFiscalYears.map(_.toString).toList))
  }

  def doSearch = Action { implicit request =>

    companyForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.searchCompanies(formWithErrors,
          findAllCompaniesNames,
          findAllCompaniesFiscalYears.map(_.toString))),
      values => {
        val name = values._1
        val year = values._2
        val out = new ByteArrayOutputStream()
        findCompaniesBy(name, year) match {
          case Some(founded : Seq[Model]) => {
            SpreadsheetWriter.write(out, founded)
            Ok(out.toByteArray()).withHeaders(CONTENT_TYPE -> "application/octet-stream",
              CONTENT_DISPOSITION -> "attachment; filename=company.xls")
          }
          case None => Ok(views.html.searchWithoutResults())
        }
      })
  }

  def companies = Action {
    Ok(views.html.companies(findAllCompanies.asInstanceOf[List[Model]]))
  }

}