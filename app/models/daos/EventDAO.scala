package models.daos

import java.util.UUID

import models.gift.Event
import models.user.User

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
trait EventDAO {

  /**
    * Check if a user is the creator of this task
    */
  def isCreator(eventid: Long, userid: UUID): Future[Boolean]

  /**
    * Finds a event by its ID.
    *
    * @return The found event or None if no user for the given ID could be found.
    */
  def find(eventId: Long): Future[Option[Event]]

  /**
    * Find all events related to a user
    */
  def findByUser(user: User): Future[List[Event]]

  /**
    * Saves a user.
    *
    * @param event The event to save.
    * @return The saved event.
    */
  def save(event: Event): Future[Event]

  def delete(id: Long)
}
