package models.daos

import java.util.UUID
import javax.inject.Inject

import models.gift.Participant
import models.user.User
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

/**
 * Give access to the user object.
 */
class ParticipantDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                                   implicit val ex: ExecutionContext) extends ParticipantDAO with DAOSlick {

  import profile.api._

  def find(eventId: Long) = {
    val participantQuery = for {
      dbParticipant <- slickParticipants.filter(p => p.eventId === eventId)
      dbUser <- slickUsers.filter(_.id === dbParticipant.userId)
    } yield (dbParticipant, dbUser)
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
            Participant.Role.fromId(p.participantRole)
          )
      }.distinct.toList
    }
  }

  def findByGiftId(giftId: Long) = {
    val participantQuery = for {
      dbGift <- slickGifts.filter(_.id === giftId)
      dbEvent <- slickEvents.filter(_.id === dbGift.eventId)
      dbParticipant <- slickParticipants.filter(p => p.eventId === dbEvent.id)
      dbUser <- slickUsers.filter(_.id === dbParticipant.userId)
    } yield (dbParticipant, dbUser)
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
          Participant.Role.fromId(p.participantRole)
        )
      }.distinct.toList
    }
  }

  def find(eventid: Long, user: User) = {
    val participantQuery = for {
      dbParticipant <- slickParticipants.filter(p => p.eventId === eventid && p.userId === user.id)
    } yield (dbParticipant)
    db.run(participantQuery.result.headOption).map { resultOption =>
      resultOption.map { case(p) =>

        Participant(
          p.id,
          user,
          p.eventId,
          Participant.Role.fromId(p.participantRole)
        )
      }
    }
  }

  def save(participant: Participant) = {
    val dbParticipant = DBParticipant(
      participant.id,
      participant.user.id,
      participant.eventid,
      Participant.Role.id(participant.role))

    val insertQuery = (slickParticipants returning slickParticipants.map(_.id)).insertOrUpdate(dbParticipant)
    dbConfig.db.run(insertQuery).map(id => participant.copy(id=id))
  }

  def insert(participants: List[Participant]) = {
    val dbParticipants = participants.map { participant =>
      DBParticipant(
        participant.id,
        participant.user.id,
        participant.eventid,
        Participant.Role.id(participant.role))
    }

    val toBeInserted = dbParticipants.map { row => (slickParticipants returning slickParticipants.map(_.id)).insertOrUpdate(row) }
    val inOneGo = DBIO.sequence(toBeInserted)
    dbConfig.db.run(inOneGo).map { _ => }
  }
}
