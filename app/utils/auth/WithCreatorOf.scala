package utils.auth

import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import models.daos.EventDAO
import models.user.User
import play.api.mvc.Request


case class WithCreatorOf[A <: Authenticator](eventDAO: EventDAO, eventid: Long) extends Authorization[User, A] {

  def isAuthorized[B](user: User, authenticator: A)(
    implicit request: Request[B]) = {

    eventDAO.isCreator(eventid, user.id)
  }
}