package controllers

import java.util.UUID

import org.joda.time.LocalDateTime
import play.api.i18n.I18nSupport
import services.user.AuthenticationEnvironment
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.user.{ Role, User }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{ AnyContent, Result }

import scala.concurrent.Future

import models.gift._

abstract class BaseController() extends Silhouette[User, CookieAuthenticator] with I18nSupport {
  def env: AuthenticationEnvironment
  
  /**
   * Redirect to login if the user in not authorized.
   */  
  private def onUnauthorized = Future.successful(Redirect(controllers.routes.AuthenticationController.signInForm))

  def withAdminSession(block: (SecuredRequest[AnyContent]) => Future[Result]) = SecuredAction.async { implicit request =>
    if (request.identity.roles.contains(Role.Admin)) {
      block(request)
    } else {
      Future.successful(NotFound("404 Not Found"))
    }
  }

  def withSession(block: (SecuredRequest[AnyContent]) => Future[Result]) = UserAwareAction.async { implicit request =>
    println("identity: " + request.identity)
    request.identity match {
      case Some(user) =>
        val secured = SecuredRequest(user, request.authenticator.getOrElse(throw new IllegalStateException()), request)
        block(secured).map { r =>
          r
        }
      case None =>
        onUnauthorized
    }
  }
  
  /**
   * Check if the connected user is the creator of an event.
   */
  def IsCreatorOf(eventid: Long)(block: (SecuredRequest[AnyContent]) => Future[Result]) = SecuredAction.async { implicit request =>    
    if (Event.isCreator(eventid, request.identity.id)) {
      block(request)
    } else {
      Future.successful(NotFound("404 Not Found"))
    }
  }
  
  def IsCreatorOfGift(giftid: Long)(block: (SecuredRequest[AnyContent]) => Future[Result]) = SecuredAction.async { implicit request =>    
    if(Gift.isCreator(giftid, request.identity.id)) {
      block(request)
    } else {
      Future.successful(NotFound("404 Not Found"))
    }
  }
  
  def IsAllowedToOffer(giftid: Long)(block: (SecuredRequest[AnyContent]) => Future[Result]) = SecuredAction.async { implicit request =>    
    if(Gift.isCreator(giftid, request.identity.id)) {
      block(request)
    } else {
      Future.successful(NotFound("404 Not Found"))
    }
  }
  
  /**
   * Check if the connected user is a participant of this event.
   */
  def IsParticipantOf(eventid: Long)(block: (SecuredRequest[AnyContent]) => Future[Result]) = SecuredAction.async { implicit request =>    
    Participant.findByEventIdAndByUserId(eventid, request.identity.id) match {
      case Some(p) => block(request)
      case _ => Future.successful(NotFound("404 Not Found"))
    }
  }
  
  /**
   * Check if the connected user is a participant of an event (from a gift id)
   */
  def IsParticipantOfWithGift(giftid: Long)(block: (SecuredRequest[AnyContent]) => Future[Result]) = SecuredAction.async { implicit request =>    
    Gift.findById(giftid) match {
      case Some(gift) => {
        Participant.findByEventIdAndByUserId(gift.event.id.get, request.identity.id) match {
          case Some(p) => block(request)
          case _ => Future.successful(NotFound("404 Not Found"))
        }
      }
      case _ => Future.successful(NotFound("404 Not Found"))
    }
  }
}

object BaseController {
  def IsOwnerOf(eventid: Long, userid: UUID) = {
    Participant.findByEventIdAndByUserId(eventid, userid) match {
      case Some(p) if p.role == Participant.Role.Owner => true
      case _ => false
    }
  }
}
