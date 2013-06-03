package controllers

import play.api.templates.Html
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.api._
import java.io.ByteArrayOutputStream
import util.Closeables
import util.FileManager._
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
import views.html.defaultpages.badRequest

//No content-negotiation yet. Just assume HTML for now
object Application extends Controller with WorkbookZipReader with SpreadsheetUploader {

  import model.mapping.ExecutivesSVTBSDilutionMapping._
  import model.mapping.ExecutivesTop5Mapping._
  import model.mapping.ExecutivesGuidelinesMapping._

  implicit val db = MongoClient()("windsor")
  
  val readersAndValidSuffixes = 
    Seq((CompanyFiscalYearReader, "Exec Top5 and Grants.xls"),
        (GuidelineReader, "Exec Top5 ST Bonus and Exec Guidelines.xls"),
        (SVTBSDilutionReader, "Company SVT BS Dilution.xls"))
        
  val companyForm = Form(
    tuple(
      "Company Name" -> nonEmptyText,
      "Company Fiscal Year" -> nonEmptyText))

  def index = Action {
    Redirect(routes.Application.companies)
  }
  
  def newCompanies = UploadSpreadsheetAction { (request,  dataset) =>
      val results = readZipFileEntries(dataset.ref.file.getAbsolutePath, readersAndValidSuffixes)

      if (results.exists { case (_, result) => result.hasErrors })
        request match {
          case Accepts.Html() => {
            val errors =
              results.filter { case (_, result) => result.hasErrors }
                .map {
                case (entryName, result) => (entryName, result.errors)
              }.toSeq
            BadRequest(views.html.parsingError(errors))
          }
          case Accepts.Json() => BadRequest("")
        }
      else {
        results.foreach { case (_, result) => result.foreach(company => updateCompany(company.get)) }
        request match {
          case Accepts.Html() => Ok(views.html.companyUploadSuccess())
          case Accepts.Json() => Ok("")
        }
      }
  }

  def newCompany = uploadSingleSpreadsheet(CompanyFiscalYearReader)
  def newExecGuideline = uploadSingleSpreadsheet(GuidelineReader)
  def newSVTBSDilution = uploadSingleSpreadsheet(SVTBSDilutionReader)

  def uploadSingleSpreadsheet(reader: WorkbookReader[Seq[Validated[Model]]]) =
    UploadSpreadsheetAction { (request, dataset) =>

      val result = reader.read(dataset.ref.file.getAbsolutePath)
      
      if (result.hasErrors)
        request match {
          case Accepts.Html() => BadRequest(views.html.parsingError(Seq((dataset.ref.file.getName, result.errors))))
          case Accepts.Json() => BadRequest("")
        }
      else {
        result.foreach(company => updateCompany(company.get))
        request match {
          case Accepts.Html() => Ok(views.html.companyUploadSuccess())
          case Accepts.Json() => Ok("")
        }
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