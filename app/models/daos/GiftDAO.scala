package models.daos

import java.util.UUID

import models.gift.Gift

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
trait GiftDAO {

  /**
    * Check if a gift is the creator of this task
    */
  def isCreator(giftId: Long, userid: UUID): Future[Boolean]

  /**
    * Finds a gift by its ID.
    *
    * @return The found event or None if no gift for the given ID could be found.
    */
  def find(giftId: Long): Future[Option[Gift]]
  def findByEventId(eventId: Long): Future[List[Gift]]


  /**
    * Saves a gift.
    *
    * @param gift The gift to save.
    * @return The saved gift.
    */
  def save(gift: Gift): Future[Gift]

  def delete(id: Long)
}
