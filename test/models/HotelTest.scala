package models

import org.scalatestplus.play.PlaySpec

class HotelTest extends PlaySpec {

  "Hotel#searchHotelByCity" should {

    "return all hotels from fixer data with city" in {
      val hotels = Hotel.searchHotelByCity("Amsterdam", page = 1, size = 10)
      hotels.size mustBe 6
      hotels.items.size mustBe 6
      hotels.items.head.city mustBe "Amsterdam".toLowerCase
    }

    "return hotel values in descending order" in {
      val hotels = Hotel.searchHotelByCity("Amsterdam", page = 1, size = 10, sorting = "DESC")
      hotels.items.head.price must be >= hotels.items.last.price
    }
  }
}
