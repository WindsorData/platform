package controllers

import play.api.libs.json.Json._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

import com.mongodb.casbah.MongoClient

import java.io.ByteArrayOutputStream

import persistence._
import model.mapping._
import output._
import util.FileManager._

import libt.error._
import libt.workflow._
import libt._


//No content-negotiation yet. Just assume HTML for now
object Application extends Controller with WorkbookZipReader with SpreadsheetUploader {

  val YearRanges = List(1, 2, 3)
  implicit val db = MongoClient()("windsor")
  
  val readersAndValidSuffixes = 
    Seq((top5.Workflow, "Exec Top5 and Grants.xlsx"),
        (guidelines.Workflow, "Exec Top5 ST Bonus and Exec Guidelines.xlsx"),
        (dilution.Workflow, "Company SVT BS Dilution.xlsx"))
        
  val companyForm = Form(
    tuple(
      "checkMe" -> seq(text),
      "Company Fiscal Year" -> number))

  def index = Action {
    Redirect(routes.Application.companies)
  }
  
  def newCompany = uploadSingleSpreadsheet(top5.Workflow)
  def newExecGuideline = uploadSingleSpreadsheet(guidelines.Workflow)
  def newSVTBSDilution = uploadSingleSpreadsheet(dilution.Workflow)

  def newCompanies =
    UploadAndReadAction {
      (request, dataset) => keyed.flatJoin(readZipFileEntries(dataset.ref.file.getAbsolutePath, readersAndValidSuffixes))
    }

  def uploadSingleSpreadsheet(reader: FrontPhase[Seq[Validated[Model]]]) =
    UploadAndReadAction {
      (request, dataset) => keyed.flatJoin(Seq(dataset.filename -> reader.readFile(dataset.ref.file.getAbsolutePath)))
    }
  
  def UploadAndReadAction(readOp: (UploadRequest, UploadFile) => keyed.Validated[Model]) = UploadSpreadsheetAction { (request, dataset) =>
    readOp(request, dataset) match {
      case Invalid(errors@_*) =>
        request match {
          case Accepts.Html() => BadRequest(views.html.parsingError(errors))
          case Accepts.Json() => BadRequest(toJson(errorsToJson(errors)))
        }
      case result => {
        result.get.foreach(updateCompany(_))
        request match {
          case Accepts.Html() => Ok(views.html.companyUploadSuccess(result.messages))
          case Accepts.Json() => Ok("")
        }
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
      YearRanges))
  }

  def doSearch = Action { implicit request =>
    companyForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.searchCompanies(formWithErrors,
          findAllCompaniesNames,
          YearRanges)),
      values => {
        val names = values._1
        val range = values._2
        val out = new ByteArrayOutputStream()
        findCompaniesBy(names, range) match {
          case Some(founded: Seq[Model]) => {
            SpreadsheetWriter.write(out, founded, range)
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
