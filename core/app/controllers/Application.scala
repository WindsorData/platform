package controllers

import play.api.templates.Html
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.api._
import java.io.ByteArrayOutputStream
import util.Closeables
import util.FileManager._
import util.ErrorHandler._
import com.mongodb.casbah.MongoClient
import com.mongodb.DBObject
import output.SpreadsheetWriter
import persistence._
import model._
import libt.error._
import libt._
import libt.spreadsheet.reader.WorkbookReader
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile

//No content-negotiation yet. Just assume HTML for now
object Application extends Controller with WorkbookZipReader[Seq[ModelOrErrors]] with SpreadsheetUploader {

  import model.mapping.ExecutivesTop5Mapping._
  import model.mapping.ExecutivesGuidelinesMapping._

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
              case (entryName, result) => (entryName, result.errors)
            }.toSeq

        response = BadRequest(views.html.parsingError(errors))
      } else {
        results.foreach { case (_, result) => result.foreach(company => updateCompany(company.get)) }
      }
      response
  }

  def newCompany = uploadSingleSpreadsheet(CompanyFiscalYearReader)
  def newExecGuideline = uploadSingleSpreadsheet(GuidelineReader)

  def uploadSingleSpreadsheet(reader: WorkbookReader[Seq[libt.ModelOrErrors]]) =
    UploadSpreadsheetAction { dataset =>
      var response = Ok(views.html.companyUploadSuccess())

      val result = reader.read(dataset.ref.file.getAbsolutePath)

      if (result.hasErrors) {
        response = BadRequest(
          views.html.parsingError(Seq((dataset.ref.file.getName, result.errors))))
      } else {
        result.foreach(company => updateCompany(company.get))
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