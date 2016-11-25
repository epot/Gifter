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
  object Status extends Enumeration {
    val New = Value(1, Messages.get("gift.statut.new"))
    val AboutToBeBought = Value(2, Messages.get("gift.statut.abouttobebought"))
    val Bought = Value(3, Messages.get("gift.statut.bought"))
    val MarkedForDeletion = Value(4, Messages.get("gift.statut.markedfordeletion"))
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
