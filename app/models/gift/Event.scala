package models.gift

import models.user._

import org.joda.time.DateTime

case class EventSimple(
  name: String,
  date: DateTime,
  eventtype: Event.Type.Value
)

case class Event(
  id: Option[Long] = None,
  creator: User,
  name: String,
  date: DateTime,
  eventType: Event.Type.Value
  ) {

  def isOwner(user: User) = creator == user
}

object Event {
  object Type extends Enumeration {
    val Birthday = Value(1)
    val Christmas = Value(2)
  }
}
