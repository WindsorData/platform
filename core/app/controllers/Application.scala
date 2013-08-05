package controllers

import org.apache.poi.ss.usermodel.WorkbookFactory
import play.api.libs.json.Json._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import persistence._
import model.mapping._
import util.FileManager._
import libt.spreadsheet.reader.workflow._
import libt.error._
import libt._
import controllers.generic._

object Application extends Controller with WorkbookZipReader with SpreadsheetUploader with SpreadsheetDownloader {

  val YearRanges = List(1, 2, 3)

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
  
  def newCompany = uploadSingleSpreadsheet(top5.Workflow)(ExecutivesDb)
  def newExecGuideline = uploadSingleSpreadsheet(guidelines.Workflow)(ExecutivesDb)
  def newSVTBSDilution = uploadSingleSpreadsheet(dilution.Workflow)(ExecutivesDb)
  def newBod = uploadSingleSpreadsheet(bod.Workflow)(BodDb)
  def newPeers = uploadSingleSpreadsheet(peers.Workflow)(PeersDb)

  def newCompanies =
    UploadAndReadAction(ExecutivesDb) {
      data => keyed.Validated.flatConcat(readZipFileEntries(data.file().getAbsolutePath))
    }

  def uploadSingleSpreadsheet(reader: FrontPhase[Seq[Model]])(db: Persistence) =
    UploadAndReadAction(db) {
      (uploadData : UploadData) => {
        val file = uploadData.file()
        val originalFilename = uploadData.originalName()
        val workbook = WorkbookFactory.create(file)
        keyed.Validated.flatConcat(Seq((originalFilename -> ticker(workbook), reader.readFile(file.getAbsolutePath))))
      }
    }

  def UploadAndReadAction(db: Persistence)(readOp: UploadData => keyed.Validated[FileAndTicker, Model]) =
    UploadSpreadsheetAction {
      case data @ UploadData(request, dataset) =>
        readOp(data) match {
          case Invalid(errors@_*) =>
            request match {
              case Accepts.Html() => BadRequest(views.html.parsingError(errors))
              case Accepts.Json() => BadRequest(toJson(messagesToJson(errors)))
            }
          case result => {
            db.update(result.get: _*)
            request match {
              case Accepts.Html() => Ok(views.html.companyUploadSuccess(result.messages))
              case Accepts.Json() => Ok(toJson(messagesToJson(result.messages)))
            }
          }
        }
    }

  def messagesToJson(errors: Seq[keyed.KeyedMessage[FileAndTicker]]) =
    Map("results" -> errors.map { case ((file, ticker), messages) => Map("file" -> toJson(file), "ticker" -> toJson(ticker) , "messages" -> toJson(messages)) })

  def reports = Action {
    Ok(views.html.reports())
  }

  def searchCompany = Action {
    Ok(views.html.searchCompanies(companyForm,
      (ExecutivesDb.allCompanies,ExecutivesDb.allCompanies) :: ExecutivesDb.findAllCompaniesId.toList,
      YearRanges))
  }

  def doSearch = Action { implicit request =>
    companyForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.searchCompanies(formWithErrors, ExecutivesDb.findAllCompaniesId, YearRanges)),
      success = values => values match {
        case (names, range) =>
          createSpreadsheetResult(names, range)(ExecutivesDb) match {
            case Some(response) => response
            case None => Ok(views.html.searchWithoutResults())
          }
      })
  }

  def companies = Action {
    Ok(views.html.companies(ExecutivesDb.findAll.toList))
  }

}
