package utils.auth

import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import models.daos.ParticipantDAO
import models.user.User
import play.api.mvc.Request
import play.api.libs.concurrent.Execution.Implicits.defaultContext

case class WithParticipantOf[A <: Authenticator](participantDAO: ParticipantDAO, eventid: Long) extends Authorization[User, A] {

  def isAuthorized[B](user: User, authenticator: A)(
    implicit request: Request[B]) = {

    participantDAO.find(eventid, user.id).map { p =>
      p.isDefined
    }
  }
}