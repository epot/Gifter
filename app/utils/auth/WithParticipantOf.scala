package utils.auth

import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import models.daos.ParticipantDAO
import models.user.User
import play.api.mvc.Request

import scala.concurrent.ExecutionContext

case class WithParticipantOf[A <: Authenticator](participantDAO: ParticipantDAO, eventid: Long)(implicit ec: ExecutionContext) extends Authorization[User, A] {

  def isAuthorized[B](user: User, authenticator: A)(
    implicit request: Request[B]) = {

    participantDAO.find(eventid, user).map { p =>
      p.isDefined
    }
  }
}