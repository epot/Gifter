package models

import java.util.UUID

import models.gift.Gift.{GiftContent, GiftWithNotification}
import models.gift._
import models.user.User
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

object JsonFormat {
  val customDateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'" // ISO 8601
  val dateFormatPattern = DateTimeFormat.forPattern(customDateFormat)
  implicit val customDateReads = JodaReads.jodaDateReads(customDateFormat)
  implicit val customDateWrites = JodaWrites.jodaDateWrites(customDateFormat)

  implicit val userWrites = new Writes[User] {
    def writes(user: User) = Json.obj(
      "id" -> user.id,
      "avatarURL" -> user.avatarURL,
      "email" -> user.email,
      "userName" -> user.userName,
      "firstName" -> user.firstName,
      "lastName" -> user.lastName,
      "fullName" -> user.fullName
    )
  }
  implicit val userReads = Json.reads[User]

  implicit val eventFormat = Json.format[Event]
  implicit val giftContentFormat = Json.format[GiftContent]
  implicit val giftFormat = Json.format[Gift]
  implicit val giftWithNotificationFormat = Json.format[GiftWithNotification]
  implicit val participantFormat = Json.format[Participant]
  implicit val commentFormat = Json.format[Comment]
  implicit val historyFormat = Json.format[History]
}
