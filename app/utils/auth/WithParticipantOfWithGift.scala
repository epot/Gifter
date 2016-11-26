package utils.auth

import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import models.daos.{GiftDAO, ParticipantDAO}
import models.user.User
import play.api.mvc.Request

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

case class WithParticipantOfWithGift[A <: Authenticator](giftDAO: GiftDAO, participantDAO: ParticipantDAO, giftid: Long) extends Authorization[User, A] {

  def isAuthorized[B](user: User, authenticator: A)(
    implicit request: Request[B]) = {

    giftDAO.find(giftid).flatMap { case(maybeGift) =>
      maybeGift match {
        case Some(gift) => {
          participantDAO.find(gift.eventid , user.id).map { maybeParticipant =>
            maybeParticipant match {
              case Some(p) => true
              case _ => false
            }
          }
        }
        case _ => Future.successful(false)
      }
    }
  }
}