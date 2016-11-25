package models.daos

import java.util.UUID
import javax.inject.Inject

import models.gift.Participant
import models.user.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Give access to the user object.
 */
class ParticipantDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends ParticipantDAO with DAOSlick {

  import driver.api._

  def find(eventId: Long) = {
    val participantQuery = for {
      dbGift <- slickParticipants.filter(p => p.eventId === eventId)
      dbUser <- slickUsers.filter(_.id === dbGift.userId)
    } yield (dbGift, dbUser)
    db.run(participantQuery.result).map { resultOption =>
      resultOption.map { case(p, u) =>
        val user = User(
          u.userID,
          Nil,
          u.firstName,
          u.lastName,
          u.fullName,
          u.email,
          u.avatarURL)

          Participant(
            p.id,
            user,
            p.eventId,
            Participant.Role(p.participantRole)
          )
      }.toList
    }
  }

  def find(eventid: Long, userid: UUID) = {
    val participantQuery = for {
      dbGift <- slickParticipants.filter(p => p.eventId === eventid && p.userId == userid)
      dbUser <- slickUsers.filter(_.id === dbGift.userId)
    } yield (dbGift, dbUser)
    db.run(participantQuery.result.headOption).map { resultOption =>
      resultOption.map { case(p, u) =>
        val user = User(
          u.userID,
          Nil,
          u.firstName,
          u.lastName,
          u.fullName,
          u.email,
          u.avatarURL)

        Participant(
          p.id,
          user,
          p.eventId,
          Participant.Role(p.participantRole)
        )
      }
    }
  }

  def save(participant: Participant) = {
    val dbParticipant = DBParticipant(
      participant.id,
      participant.user.id,
      participant.eventid,
      participant.role.id)

    val insertQuery = (slickParticipants returning slickParticipants.map(_.id)).insertOrUpdate(dbParticipant)
    dbConfig.db.run(insertQuery).map(id => participant.copy(id=id))
  }
}
