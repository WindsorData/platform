package controllers.generic

import play.api.libs.Files._
import play.api.mvc.MultipartFormData._
import play.api.mvc._
import controllers.routes
import java.io.File

trait SpreadsheetUploader { self: Controller =>
  type UploadRequest = Request[MultipartFormData[TemporaryFile]]
  type UploadFile = FilePart[TemporaryFile]

  case class UploadData(request: UploadRequest, uploadFile: UploadFile) {
    def file() : File = uploadFile.ref.file
    def originalName() : String = request.body.dataParts.getOrElse("filename", Seq(uploadFile.filename)).head
  }

  def UploadSpreadsheetAction[A](block: UploadData => Result) =
    Action(parse.multipartFormData) { request =>
      request.body.file("dataset")
        .map {
          data => block(UploadData(request, data))
        }.getOrElse {
          Redirect(routes.Application.companies).flashing("error" -> "Missing file")
        }
    }
}
