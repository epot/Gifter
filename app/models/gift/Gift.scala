package models.gift

import java.util.UUID
import models.user._

import play.api.libs.json._
import org.joda.time.DateTime

import play.i18n.Messages

case class GiftSimple(
  id: Option[Long] = None,
  creatorid: UUID,
  eventid: Long,
  creationDate: DateTime = DateTime.now,
  name: String,
  status: Gift.Status.Value = Gift.Status.New,
  toid: Option[UUID] = None,
  fromid: Option[UUID] = None,
  urls: List[String]=Nil)

case class Gift(
  id: Option[Long] = None,
  creator: User,
  eventid: Long,
  creationDate: DateTime = DateTime.now,
  name: String,
  status: Gift.Status.Value = Gift.Status.New,
  to: Option[User] = None,
  from: Option[User] = None,
  urls: List[String]=Nil)

object Gift {
  case class GiftWithNotification(gift: Gift, hasCommentNotification: Boolean)

  object Status extends Enumeration {
    val New = Value(1)
    val AboutToBeBought = Value(2)
    val Bought = Value(3)
    val MarkedForDeletion = Value(4)
  }

  case class GiftContent(
    name: String,
    status: Int,
    to: Option[UUID],
    from: Option[UUID],
    urls: List[String]
    )

  case class BaseGift(
    id: Option[Long] = None,
    creator: User,
    eventid: Long,
    creationDate: DateTime,
    content: String)

  implicit val GiftContentReads = Json.reads[GiftContent]
  implicit val GiftContentWrites = Json.writes[GiftContent]
}
