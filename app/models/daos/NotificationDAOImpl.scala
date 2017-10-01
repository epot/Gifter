package models.daos

import javax.inject.Inject

import models.gift.Notification
import models.user.User
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

/**
 * Give access to the user object using Slick
 */
class NotificationDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                                    implicit val ex: ExecutionContext) extends NotificationDAO with DAOSlick {

  import profile.api._

  def hasNotification(user: User, category: Notification.Category, objectid: Long) = {
    val notifQuery = for {
      dbNotif <- slickNotification.filter(n => n.userId === user.id
        && n.objectId === objectid
        && n.category === Notification.Category.id(category))
    } yield dbNotif
    db.run(notifQuery.result.headOption).map { dbNotif =>
      dbNotif.isDefined
    }
  }

  def delete(user: User, category: Notification.Category, objectid: Long) {
    val q = slickNotification.filter(n => n.userId === user.id
      && n.objectId === objectid
      && n.category === Notification.Category.id(category))
    db.run(q.delete)
  }

}
