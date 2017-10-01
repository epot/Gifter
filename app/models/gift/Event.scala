package models.gift

import models.user._
import org.joda.time.DateTime
import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

case class EventSimple(
  name: String,
  date: DateTime,
  eventtype: Event.Type
)

case class Event(
  id: Option[Long] = None,
  creator: User,
  name: String,
  date: DateTime,
  eventType: Event.Type
  ) {

  def isOwner(user: User) = creator == user
}

object Event {
  sealed trait Type extends EnumEntry
  object Type extends Enum[Type] with PlayJsonEnum[Type] {
    val values = findValues

    case object Birthday extends Type
    case object Christmas extends Type

    def min = 1
    def max = 2

    def id(t: Type): Int = {
      t match {
        case Birthday => 1
        case Christmas => 2
      }
    }
    def fromId(id: Int) = {
      id match {
        case 1 => Birthday
        case 2 => Christmas
      }
    }
  }
}
