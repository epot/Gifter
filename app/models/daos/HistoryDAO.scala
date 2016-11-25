package models.daos

import models.gift.History

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
trait HistoryDAO {

  def findByCategoryAndId(category: String, objectid: Long): Future[List[History]]

  def save(history: History): Future[History]
}
