package controllers

import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile
import play.api.templates.Html

import play.api.libs.json.Json._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.api._

import views.html.defaultpages.badRequest

import com.mongodb.casbah.MongoClient
import com.mongodb.DBObject

import java.io.ByteArrayOutputStream

import persistence._
import model.mapping.ExecutivesSVTBSDilutionMapping._
import model.mapping.ExecutivesTop5Mapping._
import model.mapping.ExecutivesGuidelinesMapping._
import model._
import output._
import util.Closeables
import util.FileManager._

import libt.spreadsheet.reader._
import libt.error._
import libt._


//No content-negotiation yet. Just assume HTML for now
object Application extends Controller with WorkbookZipReader with SpreadsheetUploader {


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
  
  def newCompany = uploadSingleSpreadsheet(CompanyFiscalYearReader)
  def newExecGuideline = uploadSingleSpreadsheet(GuidelineReader)
  def newSVTBSDilution = uploadSingleSpreadsheet(SVTBSDilutionReader)

  def newCompanies =
    UploadAndReadAction {
      (request, dataset) => keyed.flatJoin(readZipFileEntries(dataset.ref.file.getAbsolutePath, readersAndValidSuffixes))
    }

  def uploadSingleSpreadsheet(reader: WorkbookReader[Seq[Validated[Model]]]) =
    UploadAndReadAction {
      (request, dataset) => keyed.flatJoin(Seq(dataset.ref.file.getName -> reader.read(dataset.ref.file.getAbsolutePath)))
    }
  
  def UploadAndReadAction(readOp: (UploadRequest, UploadFile) => keyed.Validated[Model]) = UploadSpreadsheetAction { (request, dataset) =>
      val result = readOp(request, dataset)
      if (result.isInvalid) {
        request match {
          case Accepts.Html() => BadRequest(views.html.parsingError(result.toErrorSeq))
          case Accepts.Json() => BadRequest(toJson(errorsToJson(result.toErrorSeq)))
        }
      } else {
        result.get.foreach(updateCompany(_))
        request match {
          case Accepts.Html() => Ok(views.html.companyUploadSuccess())
          case Accepts.Json() => Ok("")
        }
      }
  }

  def errorsToJson(errors: Seq[keyed.KeyedMessage]) =
    errors.map {
        fileResults =>
          Map("file" -> toJson(fileResults._1), "errors" -> toJson(fileResults._2))
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