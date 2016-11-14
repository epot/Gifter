package utils.auth

import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import models.gift.Event
import models.user.User
import play.api.mvc.Request

import scala.concurrent.Future

case class WithCreatorOf[A <: Authenticator](eventid: Long) extends Authorization[User, A] {

  def isAuthorized[B](user: User, authenticator: A)(
    implicit request: Request[B]) = {

    Future.successful(Event.isCreator(eventid, user.id))
  }
}