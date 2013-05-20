package controllers

import play.api.libs.Files._
import play.api.mvc.MultipartFormData._
import play.api.mvc._
import libt.spreadsheet.reader.WorkbookReader
import util.ErrorHandler._
import util.FileManager._
import persistence._

trait SpreadsheetUploader { self: Controller =>
  def UploadSpreadsheetAction[A](block: FilePart[TemporaryFile] => Result) = Action(parse.multipartFormData) {
    request =>
      request.body.file("dataset").map(block).getOrElse {
        Redirect(routes.Application.companies).flashing("error" -> "Missing file")
      }
  }
}
