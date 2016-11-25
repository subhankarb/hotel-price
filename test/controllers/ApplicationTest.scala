package controllers

import models.ApiKey
import play.api.libs.json.Json
import play.api.libs.ws.WS
import scala.concurrent.Future

import org.scalatestplus.play._

import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class ApplicationTest extends PlaySpec with Results with OneServerPerSuite{


	"ApplicationController#welcome" should {
    	"return 200 status on index page" in {
      		val controller = new ApplicationController()
      		val result: Future[Result] = controller.welcome().apply(FakeRequest())
      		status(result) mustEqual OK
    	}
  	}

	"ApplicationController#getApiKey" should {
		"return api key on success" in {
		  	val controller = new ApplicationController()
		  	val result: Future[Result] = controller.getApiKey(Option(1)).apply(FakeRequest())
		  	status(result) mustEqual OK
		  	val responseKey = (contentAsJson(result) \ "message").as[ApiKey]
		  	responseKey.rate mustBe 1
		}
	}

	"ApplicationController#getHotelPriceByCity" should {
		val Localhost = "http://localhost:"

		"return bad request if no api key in header" in {
			val response = await(WS.url(Localhost + port + "/hotels").get())
			response.status mustBe 400
			val responseMessage = (Json.parse(response.body) \ "message").as[String]
			responseMessage mustBe "no api key present"
		}

		"return bad request if no api key in db" in {
			val response = await(WS.url(Localhost + port + "/hotels").withHeaders(("ApiKey", "key")).get())
			response.status mustBe 400
			val responseMessage = (Json.parse(response.body) \ "message").as[String]
			responseMessage mustBe "no such api key"
		}

		"return bad request if no city name present in query string" in {
			val controller = new ApplicationController()
			val result: Future[Result] = controller.getApiKey(Option(1)).apply(FakeRequest())
			status(result) mustEqual OK
			val responseKey = (contentAsJson(result) \ "message").as[ApiKey]

			val response = await(WS.url(Localhost + port + "/hotels").withHeaders(("ApiKey", responseKey.key)).get())
			response.status mustBe 400
			val responseMessage = (Json.parse(response.body) \ "message").as[String]
			responseMessage mustBe "city name can not be empty"
		}

		"return ok if all fine" in {
			val controller = new ApplicationController()
			val result: Future[Result] = controller.getApiKey(Option(1)).apply(FakeRequest())
			status(result) mustEqual OK
			val responseKey = (contentAsJson(result) \ "message").as[ApiKey]

			val response = await(WS.url(Localhost + port + "/hotels")
				.withQueryString(("city", "amsterdam"))
				.withHeaders(("ApiKey", responseKey.key)).get())
			response.status mustBe OK
			val responseMessage = (Json.parse(response.body) \ "total_data").as[Int]
			responseMessage mustBe 6
		}
	}
}
