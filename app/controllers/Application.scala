package controllers

import play.api._
import play.api.mvc._
import input.SpreadsheetLoader

//No content-negotiation yet. Just assume HTML for now
object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def newCompany = Action(parse.multipartFormData) { request =>
    request.body.file("dataset").map { dataset =>
      Ok("File uploaded at "+ dataset.ref.file.getAbsolutePath)
    }.getOrElse {
      Redirect(routes.Application.index).flashing("error" -> "Missing file")
    }
  }
  
  def company(id: Long) = TODO
  
  def companies = TODO

}