package utils.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import models.daos.ParticipantDAO
import models.gift.Participant
import models.user.User
import play.api.mvc.Request

object WithOwnerOf {
  def IsOwnerOf(participantDAO: ParticipantDAO, eventid: Long, userid: UUID) = {
    participantDAO.find(eventid, userid).map { maybeParticipant =>
      maybeParticipant match {
        case Some(p) if p.role == Participant.Role.Owner => true
        case _ => false
      }
    }
  }
}

case class WithOwnerOf[A <: Authenticator](participantDAO: ParticipantDAO, eventid: Long) extends Authorization[User, A] {

  def isAuthorized[B](user: User, authenticator: A)(
    implicit request: Request[B]) = {
    WithOwnerOf.IsOwnerOf(participantDAO, eventid, user.id)
  }
}