package controllers

import play.api.libs.Files._
import play.api.mvc.MultipartFormData._
import play.api.mvc._

trait SpreadsheetUploader { self: Controller =>
  def UploadSpreadsheetAction[A](block: FilePart[TemporaryFile] => Result) = Action(parse.multipartFormData) {
    request =>
      request.body.file("datasets").map(block).getOrElse {
        Redirect(routes.Application.companies).flashing("error" -> "Missing file")
      }
  }
}
