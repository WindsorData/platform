package controllers

import play.api._
import play.api.mvc._
import input.SpreadsheetLoader
import java.io.InputStream
import util.Closeables
import util.FileManager
import java.io.FileInputStream

//No content-negotiation yet. Just assume HTML for now
object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def newCompany = Action(parse.multipartFormData) { request =>
    request.body.file("dataset").map { dataset =>
      val executives = FileManager.loadSpreadsheet(dataset.ref.file.getAbsolutePath)
      Ok("File uploaded at " + dataset.ref.file.getAbsolutePath + "\n\n with content: " + executives.toString)
    }.getOrElse {
      Redirect(routes.Application.index).flashing("error" -> "Missing file")
    }
  }

  def company(id: Long) = TODO

  def companies = TODO

}