package models.gift

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import models.user._

case class Notification(
  id: Option[Long] = None,
  objectid: Long,
  user: User,
  category: Comment.Category)

object Notification {

  sealed trait Category extends EnumEntry
  object Category extends Enum[Category] with PlayJsonEnum[Category] {
    val values = findValues
    case object GiftComment extends Category

    def id(category: Category): Int = {
      category match {
        case GiftComment => 1
      }
    }
    def fromId(id: Int) = {
      id match {
        case 1 => GiftComment
      }
    }
  }
}