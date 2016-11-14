package utils.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import models.gift.Participant
import models.user.User
import play.api.mvc.Request

import scala.concurrent.Future

object WithOwnerOf {
  def IsOwnerOf(eventid: Long, userid: UUID) = {
    Participant.findByEventIdAndByUserId(eventid, userid) match {
      case Some(p) if p.role == Participant.Role.Owner => true
      case _ => false
    }
  }
}

case class WithOwnerOf[A <: Authenticator](eventid: Long) extends Authorization[User, A] {

  def isAuthorized[B](user: User, authenticator: A)(
    implicit request: Request[B]) = {

    Future.successful(WithOwnerOf.IsOwnerOf(eventid, user.id) )
  }
}