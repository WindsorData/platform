package controllers

import play.api.libs.Files._
import play.api.mvc.MultipartFormData._
import play.api.mvc._

trait SpreadsheetUploader { self: Controller =>
  type UploadRequest = Request[MultipartFormData[TemporaryFile]]
  type UploadFile = FilePart[TemporaryFile]

  def UploadSpreadsheetAction[A](block: (UploadRequest, UploadFile) => Result) =
    Action(parse.multipartFormData) { request =>
      request.body.file("dataset").map(block(request, _)).getOrElse {
        Redirect(routes.Application.companies).flashing("error" -> "Missing file")
      }
    }
}
