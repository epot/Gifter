package models.gift

import models.user._
import org.joda.time.DateTime
import play.api.libs.json._

case class Comment(
  id: Option[Long] = None,
  objectid: Long,
  user: User,
  creationDate: DateTime = DateTime.now,
  category: Comment.Category.Value,
  content: String)

object Comment {
  object Category extends Enumeration {
    val Gift = Value(1)
    val Event = Value(2)
  }

  case class CommentSimple(content: String, username: String, creationDate: DateTime)

  val customDateFormat = "dd/MM/yyyy HH:mm:ss"
  implicit val customDateReads = Reads.jodaDateReads(customDateFormat)
  implicit val customDateWrites = Writes.jodaDateWrites(customDateFormat)
  implicit val CommentSimpleFormat = Json.format[CommentSimple]
}