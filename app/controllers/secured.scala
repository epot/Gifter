package controllers

import javax.inject.Inject

import models.user._
import models.gift._
import services.user._
import anorm._
import play.api.mvc._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import play.api.db._
import play.api.Play.current
import play.api.db.evolutions.Evolutions
import org.joda.time.DateTime
import play.api.Play.current
import play.api.i18n.{MessagesApi, I18nSupport}

trait Secured {

  /**
   * Retrieve the connected user email.
   */
  private def userId(request: RequestHeader) = request.session.get("userId")

  /**
   * Redirect to login if the user in not authorized.
   */  
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(controllers.routes.HomeController.index())


  /**
   * Action for authenticated users. The bodyParser argument is to be able to specify, for example, a json parser.
   */
  def IsAuthenticated[A](bodyParser: BodyParser[A])(f: => User => Request[A] => Result) = Security.Authenticated(userId, onUnauthorized) { userId =>
   UserSearchService.retrieve(userId).value.get.toOption.get match {
      case Some(user) => Action(bodyParser)(request => f(user)(request))
      case _ => Action(bodyParser)(request => onUnauthorized(request))
    }
  }
  def IsAuthenticated(f: => User => Request[AnyContent] => Result): play.api.mvc.EssentialAction =
    IsAuthenticated(BodyParsers.parse.anyContent)(f)

  /**
   * Check if the connected user is the creator of an event.
   */
  def IsCreatorOf(eventid: Long)(f: => User => Request[AnyContent] => Result) = IsAuthenticated { user => request =>
    if(Event.isCreator(eventid, user.id)) {
      f(user)(request)
    } else {
      Results.Forbidden
    }
  }
  
  def IsCreatorOfGift(giftid: Long)(f: => User => Request[AnyContent] => Result) = IsAuthenticated { user => request =>
    if(Gift.isCreator(giftid, user.id)) {
      f(user)(request)
    } else {
      Results.Forbidden
    }
  }
  
  def IsAllowedToOffer(giftid: Long)(f: => User => Request[AnyContent] => Result) = IsAuthenticated { user => request =>
    if(Gift.isCreator(giftid, user.id)) {
      f(user)(request)
    } else {
      Results.Forbidden
    }
  }
  
  /**
   * Check if the connected user is a participant of this event.
   */
  def IsParticipantOf(eventid: Long)(f: => User => Request[AnyContent] => Result): play.api.mvc.EssentialAction = IsAuthenticated { user => request =>
    Participant.findByEventIdAndByUserId(eventid, user.id) match {
      case Some(p) => f(user)(request)
      case _ => Results.Forbidden
    }
  }
  
  /**
   * Check if the connected user is a participant of an event (from a gift id)
   */
  def IsParticipantOfWithGift(giftid: Long)(f: => User => Request[AnyContent] => Result): play.api.mvc.EssentialAction = IsAuthenticated { user => request =>
    Gift.findById(giftid) match {
      case Some(gift) => {
        Participant.findByEventIdAndByUserId(gift.event.id.get, user.id) match {
          case Some(p) => f(user)(request)
          case _ => Results.Forbidden
        }
      }
      case _ => Results.Forbidden
    }
  }
}