package models.daos

import models.gift.Participant
import models.user.User

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
trait ParticipantDAO {

  def find(eventId: Long): Future[List[Participant]]

  def find(eventid: Long, user: User): Future[Option[Participant]]

  def save(participant: Participant): Future[Participant]
}
