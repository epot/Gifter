package utils.auth

import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import models.gift.{Event, Participant}
import models.user.User
import play.api.mvc.Request

import scala.concurrent.Future

case class WithParticipantOf[A <: Authenticator](eventid: Long) extends Authorization[User, A] {

  def isAuthorized[B](user: User, authenticator: A)(
    implicit request: Request[B]) = {

    Future.successful(Participant.findByEventIdAndByUserId(eventid, user.id).isDefined)
  }
}