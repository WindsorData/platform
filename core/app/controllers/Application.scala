package controllers

import play.api.templates.Html
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.api._
import java.io.ByteArrayOutputStream
import util.Closeables
import util.ErrorHandler._
import util.FileManager._
import com.mongodb.casbah.MongoClient
import com.mongodb.DBObject
import output.SpreadsheetWriter
import persistence._
import model.mapping._
import model._
import libt._

//No content-negotiation yet. Just assume HTML for now
object Application extends Controller with WorkbookZipReader[Seq[ModelOrErrors]] with SpreadsheetUploader {

  implicit val db = MongoClient()("windsor")
  override val suffix = "Exec Top5 and Grants.xls"
  override val reader = CompanyFiscalYearReader

  val companyForm = Form(
    tuple(
      "Company Name" -> nonEmptyText,
      "Company Fiscal Year" -> nonEmptyText))

  def index = Action {
    Redirect(routes.Application.companies)
  }
  
  //TODO: repeated code with newCompany
  def newCompanies = UploadSpreadsheetAction {  dataset =>
      var response = Ok(views.html.companyUploadSuccess())

      val results = readZipFileEntries(dataset.ref.file.getAbsolutePath)

      if (results.exists { case (_, result) => result.hasErrors }) {
        val errors =
          results.filter { case (_, result) => result.hasErrors }
            .map {
              case (entryName, result) => (entryName, result.errors.flatMap(_.left.get))
            }.toSeq

        response = BadRequest(views.html.parsingError(errors))
      } else {
        results.foreach { case (_, result) => result.foreach(company => updateCompany(company.right.get)) }
      }
      response
  }

  def newCompany = UploadSpreadsheetAction { dataset =>
      var response = Ok(views.html.companyUploadSuccess())

      val result = CompanyFiscalYearReader.read(dataset.ref.file.getAbsolutePath)

      if (result.hasErrors) {
        response = BadRequest(
          views.html.parsingError(Seq((dataset.ref.file.getName, result.errors.flatMap(_.left.get)))))
      } else {
        result.foreach(company => updateCompany(company.right.get))
      }
      response
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
          case Some(founded: Seq[Model]) => {
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