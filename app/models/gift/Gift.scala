package models.gift

import java.util.UUID

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import models.user._
import org.joda.time.DateTime

case class GiftSimple(
  id: Option[Long] = None,
  creatorid: UUID,
  eventid: Long,
  creationDate: DateTime = DateTime.now,
  name: String,
  status: Gift.Status = Gift.Status.New,
  toid: Option[UUID] = None,
  fromid: Option[UUID] = None,
  urls: List[String]=Nil,
  secret: Boolean = false
)

case class Gift(
  id: Option[Long] = None,
  creator: User,
  eventid: Long,
  creationDate: DateTime = DateTime.now,
  name: String,
  status: Gift.Status = Gift.Status.New,
  to: Option[User] = None,
  from: Option[User] = None,
  urls: List[String]=Nil,
  secret: Boolean = false)

object Gift {
  case class GiftWithNotification(gift: Gift, hasCommentNotification: Boolean)

  sealed trait Status extends EnumEntry
  object Status extends Enum[Status] with PlayJsonEnum[Status] {
    val values = findValues

    case object New extends Status
    case object AboutToBeBought extends Status
    case object Bought extends Status
    case object MarkedForDeletion extends Status

    def id(s: Status): Int = {
      s match {
        case New => 1
        case AboutToBeBought => 2
        case Bought => 3
        case MarkedForDeletion => 4
      }
    }
    def fromId(id: Int) = {
      id match {
        case 1 => New
        case 2 => AboutToBeBought
        case 3 => Bought
        case 4 => MarkedForDeletion
      }
    }
  }

  case class GiftContent(
    name: String,
    status: Int,
    to: Option[UUID],
    from: Option[UUID],
    urls: List[String],
    secret: Option[Boolean]
  )

  case class BaseGift(
    id: Option[Long] = None,
    creator: User,
    eventid: Long,
    creationDate: DateTime,
    content: String)
}
