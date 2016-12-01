package models.daos

import javax.inject.Inject

import models.gift.Notification
import models.user.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Give access to the user object using Slick
 */
class NotificationDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends NotificationDAO with DAOSlick {

  import driver.api._

  def hasNotification(user: User, category: Notification.Category.Value, objectid: Long) = {
    val notifQuery = for {
      dbNotif <- slickNotification.filter(n => n.userId === user.id
        && n.objectId === objectid
        && n.category === category.id)
    } yield dbNotif
    db.run(notifQuery.result.headOption).map { dbNotif =>
      dbNotif.isDefined
    }
  }

  def delete(user: User, category: Notification.Category.Value, objectid: Long) {
    val q = slickNotification.filter(n => n.userId === user.id
      && n.objectId === objectid
      && n.category === category.id)
    db.run(q.delete)
  }

}
