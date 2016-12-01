package models.gift

import models.user._

case class Notification(
  id: Option[Long] = None,
  objectid: Long,
  user: User,
  category: Comment.Category.Value)

object Notification {
  object Category extends Enumeration {
    val GiftComment = Value(1)
  }
}