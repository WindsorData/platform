package controllers

import play.api.libs.json.Json._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

import com.mongodb.casbah.MongoDB
import com.mongodb.casbah.MongoClient

import persistence._

import model.mapping._

import util.FileManager._

import libt.spreadsheet.reader.workflow._
import libt.error._
import libt._
import controllers.generic.{WorkbookZipReader, SpreadsheetUploader, SpreadsheetDownloader}

object Application extends Controller with WorkbookZipReader with SpreadsheetUploader with SpreadsheetDownloader {

  val YearRanges = List(1, 2, 3)
  implicit val db = MongoClient()("windsor")
  implicit def saveAction(m: Model, db: MongoDB) = updateCompany(m)(db)

  override val entryReaders =
    Seq(
      EntryReader(top5.Workflow, "Exec Top5 and Grants.xlsx"),
      EntryReader(guidelines.Workflow, "Exec Top5 ST Bonus and Exec Guidelines.xlsx"),
      EntryReader(dilution.Workflow, "Company SVT BS Dilution.xlsx"))
        
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
  def newBod = uploadSingleSpreadsheet(bod.Workflow)(MongoClient()("windsor-bod"), (m, db) => updateCompany(m)(db) )
  def newPeers = uploadSingleSpreadsheet(peers.Workflow)(MongoClient()("windsor-peers"), (m, db) => updatePeers(m)(db) )

  def newCompanies =
    UploadAndReadAction(db, saveAction) {
      (request, dataset) => keyed.Validated.flatConcat(readZipFileEntries(dataset.ref.file.getAbsolutePath))
    }

  def uploadSingleSpreadsheet(reader: FrontPhase[Seq[Model]])(implicit db: MongoDB, saveAction: (Model, MongoDB) => Unit) =
    UploadAndReadAction(db, saveAction) {
      (request, dataset) => keyed.Validated.flatConcat(Seq(dataset.filename -> reader.readFile(dataset.ref.file.getAbsolutePath)))
    }

  def UploadAndReadAction(db: MongoDB, saveAction: (Model, MongoDB) => Unit)(readOp: (UploadRequest, UploadFile) => keyed.Validated[Model]) =
    UploadSpreadsheetAction {
      (request, dataset) =>
        readOp(request, dataset) match {
          case Invalid(errors@_*) =>
            request match {
              case Accepts.Html() => BadRequest(views.html.parsingError(errors))
              case Accepts.Json() => BadRequest(toJson(errorsToJson(errors)))
            }
          case result => {
            result.get.foreach(saveAction(_,db))
            request match {
              case Accepts.Html() => Ok(views.html.companyUploadSuccess(result.messages))
              case Accepts.Json() => Ok("")
            }
          }
        }
    }

  def errorsToJson(errors: Seq[keyed.KeyedMessage]) =
    Map("results" -> errors.map { case (file, errors) => Map("file" -> toJson(file), "errors" -> toJson(errors)) })

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
        BadRequest(views.html.searchCompanies(formWithErrors, findAllCompaniesNames, YearRanges)),
      success = values => values match {
        case (names, range) =>
          createSpreadsheetResult(names, range) match {
            case Some(response) => response
            case None => Ok(views.html.searchWithoutResults())
          }
      })
  }

  def companies = Action {
    Ok(views.html.companies(findAllCompanies.asInstanceOf[List[Model]]))
  }

}
