package controllers

import models.{ Hotel, ApiKey, API_KEY_RATE_PER_SEC }
import play.api.libs.json._
import play.api.mvc._

class ApplicationController extends Controller {

  def welcome = Action { implicit request =>
    Ok(Json.obj("message" -> "WELCOME TO THIS HOTEL PRICE PROJECT"))
  }

  def getApiKey(rate: Option[Int]) = Action { implicit request =>
    val key = ApiKey.insetApiKey(ApiKey(java.util.UUID.randomUUID.toString, rate.getOrElse(API_KEY_RATE_PER_SEC)))
    Ok(Json.prettyPrint(Json.obj("message" -> Json.toJson(key))))
  }

  def getHotelPriceByCity(city: Option[String], sort: Option[String], page: Option[Int], size: Option[Int]) = Action { implicit request =>
    val apiKeyValue = request.headers.get("ApiKey")
    if (apiKeyValue.isEmpty) {
      BadRequest(Json.obj("message" -> "no api key present"))
    } else {
      val apiKeyList = ApiKey.filterApiKeyByKey(apiKeyValue.get)

      if (apiKeyList.isEmpty) {
        BadRequest(Json.obj("message" -> "no such api key"))
      } else {
        val allowanceStatus = apiKeyList.head.acquireAllowance()
        if (!allowanceStatus) {
          Forbidden(Json.obj("message" -> "request limit exceeded, try after 5 minutes "))
        } else {
          if (city.isEmpty) {
            BadRequest(Json.obj("message" -> "city name can not be empty"))
          } else {
            val hotels = Hotel.searchHotelByCity(city.get, page = page.getOrElse(1),
              size = size.getOrElse(10), sorting = sort.getOrElse("ASC"))
            Ok(Json.prettyPrint(Json.obj(
              "result" -> Json.toJson(hotels.items),
              "total_data" -> hotels.total,
              "page" -> hotels.page,
              "size" -> hotels.size
            )))
          }

        }
      }
    }
  }
}
