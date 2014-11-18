package controllers

import _root_.util.FileManager
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
import controllers.generic.{WorkbookZipReader, SpreadsheetUploader, SpreadsheetDownloader}
import output.{FullTop5Writer, StandardTop5Writer, BodWriter, OutputWriter}

object Application extends Controller with WorkbookZipReader with SpreadsheetUploader with SpreadsheetDownloader {

  val YearRanges = List(1, 2, 3)

  val entryReadersCompanies =
    Seq(
      EntryReader(top5.Workflow, "Exec Top5 and Grants.xlsx"),
      EntryReader(guidelines.Workflow, "Exec Top5 ST Bonus and Exec Guidelines.xlsx"),
      EntryReader(dilution.Workflow, "Company SVT BS Dilution.xlsx"))

  val entryReadersBod = Seq(EntryReader(bod.Workflow, "BOD.xlsx"))
  val entryReadersPeers = Seq(EntryReader(peers.Workflow, "Peer_Peer_research.xlsx"))
        
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

  def newCompaniesBatch = uploadBatchSpreadSheets(entryReadersCompanies)(ExecutivesDb)
  def newBodsBatch = uploadBatchSpreadSheets(entryReadersBod)(BodDb)
  def newPeersBatch = uploadBatchSpreadSheets(entryReadersPeers)(PeersDb)

  def uploadBatchSpreadSheets(readers: Seq[EntryReader])(db: Database) =
    UploadAndReadAction(db) { data => {
        readZipFileEntries(data.file.getAbsolutePath, readers)
      }
    }

  def uploadSingleSpreadsheet(reader: FrontPhase[Seq[Model]])(db: Database) =
    UploadAndReadAction(db) {
      (uploadData : UploadData) => {
        val file = uploadData.file
        val originalFilename = uploadData.originalName

        var tickerName : String = null
        FileManager.loadFile(file.getAbsolutePath) { stream =>
          val workbook = WorkbookFactory.create(stream)
          tickerName = ticker(workbook)
        }

        Seq((originalFilename -> tickerName, reader.readFile(file.getAbsolutePath)))
      }
    }

  def UploadAndReadAction(db: Database)(readOp: UploadData => Seq[(FileAndTicker, error.Validated[Seq[Model]])]) =
    UploadSpreadsheetAction {
      case data => {
        implicit def results2MessagesResults(results: Seq[(FileAndTicker, error.Validated[Seq[Model]])]): Seq[(FileAndTicker, Seq[String])] =
          results.map { case (id, validated) => (id, validated.messages) }

        val results = safeReadOp(readOp, data)
        if (results.exists(_._2.isInvalid)) {
          data.request match {
            case Accepts.Html() => BadRequest(views.html.parsingError(results))
            case Accepts.Json() => BadRequest(toJson(messagesToJson(results)))
          }
        }
        else {
          db.update(results.map(_._2).flatMap(_.get): _*)
          data.request match {
            case Accepts.Html() => Ok(views.html.companyUploadSuccess(results))
            case Accepts.Json() => Ok(toJson(messagesToJson(results)))
          }
        }
      }

    }

  def safeReadOp(readOp: UploadData => Seq[(FileAndTicker, error.Validated[Seq[Model]])], data: UploadData) = {
    try {
      readOp(data)
    } catch {
      case e : Exception => Seq((data.originalName -> "Unknown", Invalid(e.getMessage)))
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
      YearRanges, routes.Application.doStandardSearch()))
  }

  def searchFullCompany = Action {
    Ok(views.html.searchCompanies(companyForm,
      (ExecutivesDb.allCompanies,ExecutivesDb.allCompanies) :: ExecutivesDb.findAllCompaniesId.toList,
      YearRanges, routes.Application.doFullSearch()))
  }

  def searchBod = Action {
    Ok(views.html.searchCompanies(companyForm,
      BodDb.findAllCompaniesId.toList,
      YearRanges, routes.Application.doBodSearch()))
  }

  def doSearch(writer: OutputWriter, db: CompaniesDb) = Action { implicit request =>
    companyForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.searchCompanies(formWithErrors, db.findAllCompaniesId, YearRanges, routes.Application.doStandardSearch())),
      success = values => values match {
        case (names, range) =>
          createCompanyBasedSpreadsheetResult(writer, names, range)(db) match {
            case Some(response) => response
            case None => Ok(views.html.searchWithoutResults())
          }
      })
  }

  def doStandardSearch = doSearch(StandardTop5Writer, ExecutivesDb)
  def doFullSearch = doSearch(FullTop5Writer, ExecutivesDb)
  def doBodSearch = doSearch(BodWriter, BodDb)

  def companies = Action {
    Ok(views.html.companies(ExecutivesDb.findAll.toList))
  }

}
