package utils.auth

import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import models.daos.GiftDAO
import models.user.User
import play.api.mvc.Request

case class WithAllowedToOffer[A <: Authenticator](giftDAO: GiftDAO, giftid: Long) extends Authorization[User, A] {

  def isAuthorized[B](user: User, authenticator: A)(
    implicit request: Request[B]) = {
    giftDAO.isCreator(giftid, user.id)
  }
}