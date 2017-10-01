package models.daos

import javax.inject.Inject

import models.gift.{Comment, Notification}
import models.user.User
import play.api.db.slick.DatabaseConfigProvider
import com.github.nscala_time.time.Imports._

import scala.concurrent.ExecutionContext

/**
  * Give access to the user object using Slick
  */
class CommentDAOImpl @Inject()(
  participantDAO: ParticipantDAO,
  protected val dbConfigProvider: DatabaseConfigProvider,
  implicit val ex: ExecutionContext) extends CommentDAO with DAOSlick {

  import profile.api._

  def findByCategoryAndId(category: Comment.Category, objectid: Long) = {

    val commentQuery = for {
      dbComment <- slickComment.filter(c => c.category === Comment.Category.id(category) && c.objectId === objectid)
      dbUser <- slickUsers.filter(_.id === dbComment.userId)
    } yield (dbComment, dbUser)
    db.run(commentQuery.result).map { results =>
      (for (result <- results) yield {
        val c = result._1
        val user = result._2
        val userObj = User(
          user.userID,
          Nil,
          user.firstName,
          user.lastName,
          user.fullName,
          user.email,
          user.avatarURL)

        Comment(c.id, c.objectId, userObj, c.creationDate, Comment.Category.fromId(c.category), c.content)
      }).toList.sortBy(_.creationDate)
    }
  }

  def save(c: Comment) = {
    val dbComment = DBComment(
      c.id,
      c.user.id,
      c.objectid,
      c.creationDate,
      Comment.Category.id(c.category),
      c.content)

    c.category match {
      case Comment.Category.Gift =>
        participantDAO.findByGiftId(c.objectid).map { participants =>
          val toInsert = for(p <- participants if p.user != c.user) yield {
            DBNotification(None, p.user.id, c.objectid, Notification.Category.id(Notification.Category.GiftComment))
          }
          db.run(DBIO.seq(slickNotification ++= toInsert).transactionally)
        }
      case _ => // not supported yet
    }

    val insertQuery = (slickComment returning slickComment.map(_.id)).insertOrUpdate(dbComment)
    dbConfig.db.run(insertQuery).map(id => c.copy(id=id))
  }
}
