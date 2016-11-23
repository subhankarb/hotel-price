package controllers

import play.api.libs.json.Json
import play.api.mvc.{ Action, Controller }

class Welcome extends Controller {

  def welcome = Action {
    Ok(Json.obj(
      "message" -> "WELCOME TO THIS HOTEL PROJECT"
    ))
  }
}
