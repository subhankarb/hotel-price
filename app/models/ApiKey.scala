package models

import play.api.libs.json.Json

case class ApiKey(val key: String, val rate: Int = API_KEY_RATE_PER_SEC) {
  val maxBlockedDuration = 5 * 60L
  val per = 1.0 // this will cal calculate like rate/sec as time diff is in sec

  private[models] var lastChecked = System.currentTimeMillis / 1000
  private[models] var allowance = rate * 1.0
  private[models] var blocked = false
  private[models] var lastBlockedTime = 0L

  private[models] def isBlocked: Boolean = blocked
  private[models] def unblock: Boolean = {
    if (isBlocked) {
      val current = System.currentTimeMillis / 1000
      val timePassed = current - lastBlockedTime
      if (timePassed > maxBlockedDuration) {
        blocked = false
      } else {
        return false
      }
    }
    true
  }

  private[models] def block(): Unit = {
    if (!isBlocked) {
      blocked = true
      lastBlockedTime = System.currentTimeMillis / 1000
    }
  }

  private[models] def checkAllowanceStatus(currentTimeInSec: Long): Boolean = {
    val timePassed = currentTimeInSec - lastChecked
    lastChecked = currentTimeInSec
    allowance = allowance + timePassed * (rate / per)
    if (allowance > rate) allowance = rate
    if (allowance < 1) {
      false
    } else {
      allowance = allowance - 1.0
      true
    }
  }

  def acquireAllowance(): Boolean = synchronized {
    checkAllowanceStatus(System.currentTimeMillis / 1000) match {
      case true => isBlocked match {
        case true => unblock
        case false => true
      }
      case false => isBlocked match {
        case true => false
        case false => block(); false
      }
    }
  }
}

object ApiKey {
  implicit val apiKeyFmt = Json.format[ApiKey]
  import FakeDB.apiKeys

  def insetApiKey(apiKey: ApiKey): ApiKey = {
    apiKeys.insert(_ => apiKey)._2
  }

  def filterApiKeyByKey(key: String): List[ApiKey] = {
    apiKeys.filter(f => f.key == key)
  }
}