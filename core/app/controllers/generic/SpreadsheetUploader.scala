package controllers.generic

import play.api.libs.Files._
import play.api.mvc.MultipartFormData._
import play.api.mvc._
import controllers.routes
import java.io.File

trait SpreadsheetUploader { self: Controller =>
  type UploadRequest = Request[AnyContent]
  type UploadFile = FilePart[TemporaryFile]

  trait UploadData {
    def file : File
    def originalName : String
    def request : UploadRequest
  }

  case class UploadDataByRequest(request: UploadRequest) extends UploadData {
    override def file = uploadFile.ref.file
    override def originalName = uploadFile.filename

    private def multipartForm = request.body.asMultipartFormData.get
    private def uploadFile : UploadFile = multipartForm.file("dataset").get
  }

  case class UploadDataByFileSystem(request: UploadRequest) extends UploadData {
    override def file = new File(data.get("path_fs").get.head)
    override def originalName = data.get("filename").get.head

    private def data = request.body.asFormUrlEncoded.get
  }

  def UploadSpreadsheetAction[A](block: UploadData => Result) = {
    Action (request => block(getRequestData(request)))
  }

  def getRequestData(request : UploadRequest) = {
    request.getQueryString("upload_method") match {
      case Some("fs") => UploadDataByFileSystem(request)
      case _ => UploadDataByRequest(request)
    }
  }

}
