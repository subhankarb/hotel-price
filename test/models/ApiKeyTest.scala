package models

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec

class ApiKeyTest extends PlaySpec {

  "ApiKey#acquireAllowance" should {

    "return false if checkAllowanceStatus and isBlocked and unable to unblock" in {
      val apiKey = new ApiKey("test-api-key") {
        override def isBlocked = true
        override def unblock = false
        override def checkAllowanceStatus(time: Long) = true
      }

      val acquire = apiKey.acquireAllowance()
      acquire mustBe false
    }

    "return false and get blocked if allowances finished" in {
      val apiKey = new ApiKey("test-api-key") {
        override def checkAllowanceStatus(time: Long) = false
      }
      val acquire = apiKey.acquireAllowance()
      acquire mustBe false
      apiKey.isBlocked mustBe true
    }
  }

  "ApiKey#checkAllowanceStatus" should {

    "decrease allowance by one if called in same second" in {
      val time = System.currentTimeMillis / 1000
      val apiKey = ApiKey("test-api-key")
      apiKey.checkAllowanceStatus(time)
      val allowanceOne = apiKey.allowance
      apiKey.checkAllowanceStatus(time)
      val allowanceTwo = apiKey.allowance
      allowanceOne mustEqual (allowanceTwo + 1)
    }

    "return false if called more than rate in same second" in {
      val time = System.currentTimeMillis / 1000
      val apiKey = ApiKey("test-api-key", rate = 1)
      val allowanceStatusOne = apiKey.checkAllowanceStatus(time)
      val allowanceStatusTwo = apiKey.checkAllowanceStatus(time)

      allowanceStatusOne mustBe true
      allowanceStatusTwo mustBe false
    }
  }

  "ApiKey#block" should {
    "make block status true and change last blocked time" in {
      val apiKey = ApiKey("test-api-key")
      val lastBlockedTimeInitial = apiKey.lastBlockedTime
      apiKey.block()
      val lastBlockedTimeModified = apiKey.lastBlockedTime

      lastBlockedTimeInitial should be < lastBlockedTimeModified
      apiKey.isBlocked mustBe true

    }
  }

  "ApiKey#unblock" should {

    "return false if unblock called before 5 minutes" in {
      val apiKey = ApiKey("test-api-key")
      apiKey.lastBlockedTime = (System.currentTimeMillis / 1000) - 60
      apiKey.blocked = true

      val unblockStatus = apiKey.unblock
      unblockStatus mustBe false
      apiKey.isBlocked mustBe true
    }

    "return true if unblock called after 5 minutes" in {
      val apiKey = ApiKey("test-api-key")
      apiKey.lastBlockedTime = (System.currentTimeMillis / 1000) - (60 * 10)
      apiKey.blocked = true

      val unblockStatus = apiKey.unblock
      unblockStatus mustBe true
      apiKey.isBlocked mustBe false
    }

    "return true if not blocked" in {
      val apiKey = ApiKey("test-api-key")
      val unblockStatus = apiKey.unblock
      unblockStatus mustBe true
    }
  }

  "ApiKey#Integration" should {
    "insert and filter data from database" in {
      ApiKey.insetApiKey(ApiKey("Test-API"))
      FakeDB.apiKeys.size mustBe 1
      val apiKey = ApiKey.filterApiKeyByKey("Test-API")
      apiKey.head.key mustBe "Test-API"
      FakeDB.apiKeys.deleteAll()
    }
  }
}
