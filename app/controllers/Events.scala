package controllers

import javax.inject.Inject
import java.util.UUID

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
import play.api.i18n.{MessagesApi, I18nSupport}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import services.user.AuthenticationEnvironment
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class Events @Inject() (override val messagesApi: MessagesApi, override val env: AuthenticationEnvironment) 
  extends BaseController with I18nSupport {

  val eventForm = Form[EventSimple](
    tuple(
      "creatorid" -> nonEmptyText
      ,"name" -> nonEmptyText
      ,"dateStr" -> date("dd-MM-yyyy")
      ,"type" -> nonEmptyText
      ).transform(
    {/*apply*/
      case (creatorid, name, dateStr, eventtype) => {
        EventSimple(creatorid=UUID.fromString(creatorid), name=name, date=new DateTime(dateStr), eventtype=Event.Type.withName(eventtype))
      }
    },{ /*unapply*/
      event: EventSimple => (
            event.creatorid.toString(),
            event.name,
            event.date.toDate,
            event.eventtype.toString)
    })
  )  
  
  def newEvent = withSession { s =>
    Future.successful(Ok(views.html.newEvent(s.identity, eventForm)))
  }


  def postNewEvent() = withSession { implicit request =>
    eventForm.bindFromRequest.fold(
      errors => {
        println(errors)
        Future.successful(BadRequest(views.html.newEvent(request.identity, errors)))
      },
      event => {
        UserSearchService.retrieve(event.creatorid).map {
          user => user match {
            case Some(u) => {
              val new_event = Event.create(Event(creator = u,
                name= event.name,
                date= event.date,
                eventtype= event.eventtype))
              Participant.create(Participant(event=new_event, user=request.identity, role=Participant.Role.Owner))
              Redirect(routes.HomeController.index).withSession("userId" -> request.identity.id.toString)
            }
            case None => Redirect(controllers.routes.Events.newEvent).flashing(("error", "Could not find creator."))
          }
        }
      }
    )
  }  
  
  def event(eventid: Long) = IsParticipantOf(eventid) { implicit request =>
    Future.successful(Ok(views.html.event(request.identity, Event.findById(eventid).get)))
  }

  def eventWithUser(eventid: Long, userid: UUID) = IsParticipantOf(eventid) { implicit request =>
    UserSearchService.retrieve(userid).map { to =>
      Ok(views.html.event(request.identity, Event.findById(eventid).get, to))
    }
  }

  /**
   * Delete an event.
   */
  def postDeleteEvent(eventid: Long) = IsCreatorOf(eventid) { implicit request =>
    Event.findById(eventid) match {
      case Some(event) => { 
        Event.delete(eventid)
        Future.successful(Redirect(routes.HomeController.index))
      }
      case None => Future.successful(BadRequest)
    }
  }
  
  
  val giftForm = Form[Gift] {
    tuple(
      "id" -> optional(longNumber).verifying ("Could not find gift to update.", 
          optid => optid match {
            case Some(id) => Gift.findById(id).isDefined
            case None => true 
          })
      ,"creatorid" -> nonEmptyText
      ,"eventid" -> longNumber.verifying ("Could not find event. Maybe you deleted it ?", id => Event.findById(id).isDefined)
      ,"name" -> nonEmptyText
      ,"urls" -> list(nonEmptyText)
      ,"to" -> optional(nonEmptyText)
    ).transform(
    {/*apply*/
      case (optid, creatorid, eventid, name, urls, toid) => {
        val pkid = optid match {
          case Some(x) => anorm.Id(x)
          case None => NotAssigned
        }
        val to = toid match {
          case Some(id) => UserSearchService.retrieve(id).value.get.toOption.get
          case None => None
        }
        Gift(
            id=pkid,
            creator=UserSearchService.retrieve(creatorid).value.get.toOption.get.get, 
            event=Event.findById(eventid).get, 
            name=name, 
            urls=urls,
            to = to
            )
      }
    },{ /*unapply*/
      gift: Gift => {
        val toid = gift.to match {
          case Some(user) => Some(user.id.toString)
          case None => None
        }
        ( gift.id.toOption,
          gift.creator.id.toString(),
          gift.event.id.get,
          gift.name,
          gift.urls,
          toid)
      }
    })
  }

  def newGift(eventid: Long) = withSession { implicit request =>
    Future.successful(Ok(views.html.gifts.edit_gift(request.identity, giftForm.fill(Gift(creator=request.identity, event=Event.findById(eventid).get, name="")))))
  }

  def postEditGift(eventid: Long) = IsParticipantOf(eventid) { implicit request =>
    giftForm.bindFromRequest.fold(
      errors => {
        println(errors)
        Future.successful(BadRequest(views.html.gifts.edit_gift(request.identity, errors)))
      },
      gift => {
        val newGift = gift.id.toOption match {
          case Some(id) => { 
            History.create(History(objectid=id, user=request.identity, category="Gift", content="Update gift from " + Gift.findById(id) + " to " +  gift))
            Gift.update(gift)
          }
          case None => Gift.create(gift)
        }
        
        newGift.to match {
          case Some(user_to) => Future.successful(Redirect(routes.Events.eventWithUser(newGift.event.id.get, user_to.id)).withSession("userId" -> request.identity.id.toString))
          case _ => Future.successful(Redirect(routes.Events.event(newGift.event.id.get)).withSession("userId" -> request.identity.id.toString))
        }
      }
    )
  }  
  
  def editGift(giftid: Long) = IsParticipantOfWithGift(giftid) { implicit request =>
    Future.successful(Ok(views.html.gifts.edit_gift(request.identity, giftForm.fill(Gift.findById(giftid).get))))
  }
  
  def viewGift(giftid: Long) = IsParticipantOfWithGift(giftid) { implicit request =>
    Future.successful(Ok(views.html.gifts.view_gift(request.identity, Gift.findById(giftid).get)))
  }
  
  /**
   * Delete a gift.
   */
  def postDeleteGift(giftid: Long) = IsCreatorOfGift(giftid) { implicit request =>
    Gift.findById(giftid) match {
      case Some(gift) => { 
        Gift.delete(giftid)
        Future.successful(Redirect(routes.Events.event(gift.event.id.get)).withSession("userId" -> request.identity.id.toString))
      }
      case None => Future.successful(BadRequest)
    }
  }
  
  
  def addParticipant = withSession { implicit request =>

    Events.addParticipantForm.bindFromRequest.fold(
      errors => {
        println(errors)
        Future.successful(BadRequest(views.html.participants.participants_add_form(errors)))
      },
      tuple => {
        
        val eventid = tuple._1
        val email = tuple._2
        val role = Participant.Role.withName(tuple._3)
        
        Participant.findByEventIdAndByEmail(eventid, email) match {
          case Some(participant) => {
            Participant.update(participant.id.get, role)
          }
          case None => {
            UserSearchService.retrieve(LoginInfo(CredentialsProvider.ID, email)).map {
              user => user match {
                case Some(u) =>  Participant.create(Participant(
                  user=u,
                  event=Event.findById(eventid).get,
                  role=role))
                case None =>
              }
            }
          }
        }
        
        Future.successful(Ok(views.html.participants.participants_table(request.identity, Participant.findByEventId(eventid))))
      }
    )
  }
  
  
  def updateGiftStatus(giftid: Long) = IsParticipantOfWithGift(giftid) { implicit request =>
    Form("status" -> nonEmptyText).bindFromRequest.fold(
      errors => {
        Future.successful(BadRequest)
      },
      status => {
        Gift.findById(giftid) match {
          case Some(gift) => {
            
            gift.from match {
              case Some(x) if x != request.identity => Future.successful(BadRequest)
              case _ => {
                val statusValue = Gift.Status.withName(status)

                val from = statusValue match {
                  case Gift.Status.New => None
                  case _ => Some(request.identity)
                }
                
                History.create(History(objectid=giftid, user=request.identity, category="Gift", content="Update gift status from " + gift.status + " to " +  status))
                val newGift = Gift.update(gift.copy(status=statusValue, from=from))
                
                
                    println("fucker to " + newGift)
                
                newGift.to match {
                  case Some(user_to) => {
                    Future.successful(Redirect(routes.Events.eventWithUser(newGift.event.id.get, user_to.id)).withSession("userId" -> request.identity.id.toString))
                  }
                  case _ => Future.successful(Redirect(routes.Events.event(newGift.event.id.get)).withSession("userId" -> request.identity.id.toString))
                }
              }
            }
          }
          case None => Future.successful(BadRequest)
        }
      }
    )
  }
}

object Events {
    val addParticipantForm = Form {
    tuple(
      "eventid" -> longNumber.verifying ("Could not find event. Maybe you deleted it ?", id => Event.findById(id).isDefined)
      ,"email" -> email
      ,"role" -> nonEmptyText
    )
  }
}
