package models

import org.scalatestplus.play.PlaySpec

class FakeDbTest extends PlaySpec {

  "FakeDbTest#hotels" should {

    "load hotel data from CSV at app start up" in {
      val hotelRows = FakeDB.hotels.size

      hotelRows mustBe 26
    }

    "have empty api key table" in {
      FakeDB.apiKeys.isEmpty mustBe true
    }
  }

  "FakeDb#FakeTable" should {

    "increase next id by one" in {
      val first = FakeDB.apiKeys.nextId
      first mustBe FakeDB.apiKeys.size + 1
    }

    "get element by id" in {
      FakeDB.apiKeys.insert(_ => ApiKey("TestKey"))
      FakeDB.apiKeys.size mustBe 1
      FakeDB.apiKeys.get(1L).get.key mustBe "TestKey"

      FakeDB.apiKeys.delete(1L)
      FakeDB.apiKeys.size mustBe 0
    }

    "able to filter by value" in {
      FakeDB.apiKeys.insert(_ => ApiKey("TestKey", rate = 10))
      FakeDB.apiKeys.insert(_ => ApiKey("TestKey1", rate = 9))

      val apiKey = FakeDB.apiKeys.filter(f => f.rate == 9)
      apiKey.head.rate mustBe 9
      FakeDB.apiKeys.deleteAll()
    }

    "able to find by value" in {
      FakeDB.apiKeys.insert(_ => ApiKey("TestKey", rate = 10))
      FakeDB.apiKeys.insert(_ => ApiKey("TestKey1", rate = 9))

      val apiKey = FakeDB.apiKeys.find(f => f.key == "TestKey1")

      apiKey.head.key mustBe "TestKey1"
      FakeDB.apiKeys.deleteAll()
    }

    "able to page result" in {
      FakeDB.apiKeys.insert(_ => ApiKey("TestKey", rate = 10))
      FakeDB.apiKeys.insert(_ => ApiKey("TestKey1", rate = 9))
      FakeDB.apiKeys.insert(_ => ApiKey("TestKey2", rate = 10))
      FakeDB.apiKeys.insert(_ => ApiKey("TestKey3", rate = 9))

      val data = FakeDB.apiKeys.page(1, 2)(f => f.rate > 8)((a1, a2) => a1.rate < a2.rate)
      data.items.foreach(a => println(a))
      data.items.size mustBe 2
      data.page mustBe 1
      data.total mustBe 4
      data.offset mustBe 1

      FakeDB.apiKeys.deleteAll()
    }
  }
}
