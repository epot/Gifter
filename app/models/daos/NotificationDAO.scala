package models.daos


import models.gift.Notification
import models.user.User

import scala.concurrent.Future

/**
 * Give access to the notification object.
 */
trait NotificationDAO {
  def hasNotification(user: User, category: Notification.Category, objectid: Long): Future[Boolean]
  def delete(user: User, category: Notification.Category, objectid: Long)
}
