package controllers

import javax.inject.Inject
import java.util.UUID

import play.api.data.Forms._
import models.gift._
import models.services.user._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.joda.time.DateTime
import play.api.i18n.{I18nSupport, MessagesApi}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.auth._

import scala.concurrent.Future

class Events @Inject() (val messagesApi: MessagesApi, silhouette: Silhouette[DefaultEnv])
  extends Controller with I18nSupport {

  val eventForm = Form[EventSimple](
    tuple(
      "name" -> nonEmptyText
      ,"dateStr" -> date("dd-MM-yyyy")
      ,"type" -> nonEmptyText
      ).transform(
    {/*apply*/
      case (name, dateStr, eventtype) => {
        EventSimple(name=name, date=new DateTime(dateStr), eventtype=Event.Type.withName(eventtype))
      }
    },{ /*unapply*/
      event: EventSimple => (
            event.name,
            event.date.toDate,
            event.eventtype.toString)
    })
  )  
  
  def newEvent = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.newEvent(request.identity, eventForm)))
  }


  def postNewEvent() = silhouette.SecuredAction.async { implicit request =>
    eventForm.bindFromRequest.fold(
      errors => {
        println(errors)
        Future.successful(BadRequest(views.html.newEvent(request.identity, errors)))
      },
      event => {
        val new_event = Event.create(Event(creator = request.identity,
          name= event.name,
          date= event.date,
          eventtype= event.eventtype))
        Participant.create(Participant(event=new_event, user=request.identity, role=Participant.Role.Owner))
        Future.successful(Redirect(routes.HomeController.index))
      }
    )
  }  
  
  def event(eventid: Long) = silhouette.SecuredAction(WithParticipantOf[DefaultEnv#A](eventid)).async { implicit request =>
    Future.successful(Ok(views.html.event(request.identity, Event.findById(eventid).get)))
  }

  def eventWithUser(eventid: Long, userid: UUID) = silhouette.SecuredAction(WithParticipantOf[DefaultEnv#A](eventid)).async { implicit request =>
    UserSearchService.retrieve(userid).map { to =>
      Ok(views.html.event(request.identity, Event.findById(eventid).get, to))
    }
  }

  /**
   * Delete an event.
   */
  def postDeleteEvent(eventid: Long) = silhouette.SecuredAction(WithCreatorOf[DefaultEnv#A](eventid)).async { implicit request =>
    Event.findById(eventid) match {
      case Some(event) => { 
        Event.delete(eventid)
        Future.successful(Redirect(routes.HomeController.index))
      }
      case None => Future.successful(BadRequest)
    }
  }
  
  
  val giftForm = Form[GiftSimple] {
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
        
        val pkid = optid

        val toid_uuid = toid match {
          case Some(id) => Some(UUID.fromString(id))
          case None => None
        }
        GiftSimple(
            id = pkid,
            creatorid = UUID.fromString(creatorid),
            event=Event.findById(eventid).get, 
            name=name, 
            urls=urls,
            toid = toid_uuid
            )
      }
    },{ /*unapply*/
      gift: GiftSimple => {
        val toid = gift.toid match {
          case Some(id) => Some(id.toString)
          case None => None
        }
        ( gift.id,
          gift.creatorid.toString,
          gift.event.id.get,
          gift.name,
          gift.urls,
          toid)
      }
    })
  }

  def newGift(eventid: Long) = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.gifts.edit_gift(request.identity, giftForm.fill(GiftSimple(creatorid=request.identity.id, event=Event.findById(eventid).get, name="")))))
  }

  def postEditGift(eventid: Long) = silhouette.SecuredAction(WithParticipantOf[DefaultEnv#A](eventid)).async { implicit request =>
    giftForm.bindFromRequest.fold(
      errors => {
        println(errors)
        Future.successful(BadRequest(views.html.gifts.edit_gift(request.identity, errors)))
      },
      gift => {
        
        // quickwin
        
        val to = gift.toid match {
          case Some(id) => UserSearchService.blocking_ugly_retrieve_option(id)
          case None => None
        }
        val from = gift.fromid match {
          case Some(id) => UserSearchService.blocking_ugly_retrieve_option(id)
          case None => None
        }
        
        val new_gift = Gift(
          id = gift.id,
          creator = UserSearchService.blocking_ugly_retrieve(gift.creatorid),
          event = gift.event,
          creationDate = gift.creationDate,
          name = gift.name,
          status = gift.status,
          to = to,
          from = from,
          urls = gift.urls)

        
        val newGift = gift.id match {
          case Some(id) => { 
            History.create(History(objectid=id, user=request.identity, category="Gift", content="Update gift from " + Gift.findById(id) + " to " +  new_gift))
            Gift.update(new_gift)
          }
          case None => Gift.create(new_gift)
        }
        
        newGift.to match {
          case Some(user_to) => Future.successful(Redirect(routes.Events.eventWithUser(newGift.event.id.get, user_to.id)))
          case _ => Future.successful(Redirect(routes.Events.event(newGift.event.id.get)))
        }
      }
    )
  }  
  
  def editGift(giftid: Long) = silhouette.SecuredAction(WithParticipantOfWithGift[DefaultEnv#A](giftid)).async { implicit request =>
    val gift = Gift.findById(giftid).get
    val gift_simple = GiftSimple(
          id = gift.id,
          creatorid = gift.creator.id,
          event = gift.event,
          creationDate = gift.creationDate,
          name = gift.name,
          status = gift.status,
          toid = gift.to.map{_.id},
          fromid = gift.from.map{_.id},
          urls = gift.urls)
    Future.successful(Ok(views.html.gifts.edit_gift(request.identity, giftForm.fill(gift_simple))))
  }
  
  def viewGift(giftid: Long) = silhouette.SecuredAction(WithParticipantOfWithGift[DefaultEnv#A](giftid)).async { implicit request =>
    Future.successful(Ok(views.html.gifts.view_gift(request.identity, Gift.findById(giftid).get)))
  }
  
  /**
   * Delete a gift.
   */
  def postDeleteGift(giftid: Long) = silhouette.SecuredAction(WithGiftCreatorOf[DefaultEnv#A](giftid)).async { implicit request =>
    Gift.findById(giftid) match {
      case Some(gift) => { 
        Gift.delete(giftid)
        Future.successful(Redirect(routes.Events.event(gift.event.id.get)))
      }
      case None => Future.successful(BadRequest)
    }
  }
  
  
  def addParticipant = silhouette.SecuredAction.async { implicit request =>

    Events.addParticipantForm.bindFromRequest.fold(
      errors => {
        Future.successful(BadRequest(views.html.participants.participants_add_form(errors)))
      },
      tuple => {
        
        val eventid = tuple._1
        val username = tuple._2
        val role = Participant.Role.withName(tuple._3)
        
        
        UserSearchService.retrieve(username).map {
          user => user match {
            case Some(u) =>  {
              Participant.findByEventIdAndByUserId(eventid, u.id) match {
                case Some(p) => Participant.update(p.id.get, role)
                case None => Participant.create(Participant(
                  user=u,
                  event=Event.findById(eventid).get,
                  role=role)) 
              }
              Ok(views.html.participants.participants_table(request.identity, Participant.findByEventId(eventid)))
            }
            case None => {
              val form = Events.addParticipantForm.fill(tuple)
              val moncul = form.withError("username","Could not find anyone with this username")
              println(moncul)
              BadRequest(views.html.participants.participants_add_form(form.withError("username","User name not found")))
            }
          }
        }
      }
    )
  }
  
  
  def updateGiftStatus(giftid: Long) = silhouette.SecuredAction(WithParticipantOfWithGift[DefaultEnv#A](giftid)).async { implicit request =>
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
      ,"username" -> nonEmptyText
      ,"role" -> nonEmptyText
    )
  }
}
