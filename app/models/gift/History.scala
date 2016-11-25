package models.gift

import models.user._
import org.joda.time.DateTime

case class History(
  id: Option[Long] = None,
  objectid: Long,
  user: User,
  creationDate: DateTime = DateTime.now,
  category: String,
  content: String)

