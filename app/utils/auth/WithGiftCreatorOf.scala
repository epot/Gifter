package utils.auth

import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import models.gift.{Gift}
import models.user.User
import play.api.mvc.Request

import scala.concurrent.Future

case class WithGiftCreatorOf[A <: Authenticator](giftid: Long) extends Authorization[User, A] {

  def isAuthorized[B](user: User, authenticator: A)(
    implicit request: Request[B]) = {

    Future.successful(Gift.isCreator(giftid, user.id))
  }
}