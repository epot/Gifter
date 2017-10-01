package models.gift

import java.util.UUID

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import models.user._

case class Participant(
  id: Option[Long] = None,
  user: User,
  eventid: Long,
  role: Participant.Role)

object Participant {
  sealed trait Role extends EnumEntry
  object Role extends Enum[Role] with PlayJsonEnum[Role] {
    val values = findValues

    case object Owner extends Role
    case object Gifter extends Role
    case object Reader extends Role

    def id(r: Role): Int = {
      r match {
        case Owner => 1
        case Gifter => 2
        case Reader => 3
      }
    }
    def fromId(id: Int) = {
      id match {
        case 1 => Owner
        case 2 => Gifter
        case 3 => Reader
      }
    }
  }

  case class BaseParticipant(
    id: Option[Long] = None,
    userid: UUID,
    eventid: Long,
    role: Int)

}
