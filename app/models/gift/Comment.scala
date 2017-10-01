package models.gift

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import models.user._
import org.joda.time.DateTime

case class Comment(
  id: Option[Long] = None,
  objectid: Long,
  user: User,
  creationDate: DateTime = DateTime.now,
  category: Comment.Category,
  content: String)

object Comment {
  sealed trait Category extends EnumEntry
  object Category extends Enum[Category] with PlayJsonEnum[Category] {
    val values = findValues
    case object Gift extends Category

    def id(category: Category): Int = {
      category match {
        case Gift => 1
      }
    }
    def fromId(id: Int) = {
      id match {
        case 1 => Gift
      }
    }
  }
}