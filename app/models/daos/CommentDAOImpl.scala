package models.daos

import javax.inject.Inject

import models.gift.Comment
import models.user.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.github.nscala_time.time.Imports._

/**
  * Give access to the user object using Slick
  */
class CommentDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends CommentDAO with DAOSlick {

  import driver.api._

  def findByCategoryAndId(category: Comment.Category.Value, objectid: Long) = {

    val commentQuery = for {
      dbComment <- slickComment.filter(c => c.category === category.id && c.objectId === objectid)
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

        Comment(c.id, c.objectId, userObj, c.creationDate, Comment.Category(c.category), c.content)
      }).toList.sortBy(_.creationDate)
    }
  }

  def save(c: Comment) = {
    val dbComment = DBComment(
      c.id,
      c.user.id,
      c.objectid,
      c.creationDate,
      c.category.id,
      c.content)

    val insertQuery = (slickComment returning slickComment.map(_.id)).insertOrUpdate(dbComment)
    dbConfig.db.run(insertQuery).map(id => c.copy(id=id))
  }
}
