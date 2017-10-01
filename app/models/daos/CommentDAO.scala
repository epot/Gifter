package models.daos

import models.gift.Comment

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
trait CommentDAO {

  def findByCategoryAndId(category: Comment.Category, objectid: Long): Future[List[Comment]]

  def save(comment: Comment): Future[Comment]
}
