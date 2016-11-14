package utils.auth

import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import models.gift.{Event, Gift, Participant}
import models.user.User
import play.api.mvc.Request

import scala.concurrent.Future

case class WithParticipantOfWithGift[A <: Authenticator](giftid: Long) extends Authorization[User, A] {

  def isAuthorized[B](user: User, authenticator: A)(
    implicit request: Request[B]) = {

    Future.successful(Gift.findById(giftid) match {
      case Some(gift) => {
        Participant.findByEventIdAndByUserId(gift.event.id.get, user.id) match {
          case Some(p) => true
          case _ => false
        }
      }
      case _ => false
    })
  }
}