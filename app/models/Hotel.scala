package models

import play.api.libs.json.Json
import utils.Page

case class Hotel(city: String, hotelId: Int, room: String, price: Int)

object Hotel {
  implicit val hotelFmt = Json.format[Hotel]
  import FakeDB.hotels

  def searchHotelByCity(city: String, page: Int = 1, size: Int = 3, sorting: String = "ASC"): Page[Hotel] = {
    hotels.page(page, size)(f => f.city == city.toLowerCase) { (h1, h2) =>
      sorting match { case "DESC" => h1.price > h2.price; case _ => h1.price < h2.price; }
    }
  }

}