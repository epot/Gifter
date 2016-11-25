package models.daos

import java.util.UUID

import models.gift.Participant

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
trait ParticipantDAO {

  def find(eventId: Long): Future[List[Participant]]

  def find(eventid: Long, userid: UUID): Future[Option[Participant]]

  def save(participant: Participant): Future[Participant]
}
