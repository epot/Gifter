package controllers

import models.user._
import models.gift._
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

object Events extends Controller with Secured {

  val eventForm = Form[Event](
    tuple(
      "creatorid" -> longNumber.verifying ("Could not find creator.", id => User.findById(id).isDefined)
      ,"name" -> nonEmptyText
      ,"dateStr" -> date("dd-MM-yyyy")
      ).transform(
    {/*apply*/
      case (creatorid, name, dateStr) => {
        Event(creator= User.findById(creatorid).get, name=name, date=new DateTime(dateStr))
      }
    },{ /*unapply*/
      event: Event => (
            event.creator.id.get,
            event.name,
            event.date.toDate)
    })
  )  
  
  def newEvent = IsAuthenticated { user => implicit request =>
    Ok(views.html.newEvent(user, eventForm))
  }


  def postNewEvent() = IsAuthenticated { user => implicit request =>
    eventForm.bindFromRequest.fold(
      errors => {
        println(errors)
        BadRequest(views.html.newEvent(user, errors))
      },
      event => {
        Event.create(event)
        Redirect(routes.UserApplication.index).withSession("userId" -> user.id.toString)
      }
    )
  }  
  
  def event(eventid: Long) = IsAuthenticated { user => implicit request =>
    Ok(views.html.event(user, Event.findById(eventid).get))
  }

  /**
   * Delete an event.
   */
  def postDeleteEvent(eventid: Long) = IsCreatorOf(eventid) { user => implicit request =>
    Event.findById(eventid) match {
      case Some(event) => { 
        Event.delete(eventid)
        Redirect(routes.UserApplication.index).withSession("userId" -> user.id.toString)
      }
      case None => BadRequest
    }
  }
  
  val addParticipantForm = Form {
    tuple(
      "eventid" -> longNumber.verifying ("Could not find event. Maybe you deleted it ?", id => Event.findById(id).isDefined)
      ,"email" -> email
      ,"role" -> nonEmptyText
    )
  }
  
  val giftForm = Form[Gift] {
    tuple(
      "id" -> optional(longNumber).verifying ("Could not find gift to update.", 
          optid => optid match {
            case Some(id) => Gift.findById(id).isDefined
            case None => true 
          })
      ,"creatorid" -> longNumber.verifying ("Could not find creator.", id => User.findById(id).isDefined)
      ,"eventid" -> longNumber.verifying ("Could not find event. Maybe you deleted it ?", id => Event.findById(id).isDefined)
      ,"name" -> nonEmptyText
      ,"urls" -> list(nonEmptyText)
    ).transform(
    {/*apply*/
      case (optid, creatorid, eventid, name, urls) => {
        val pkid = optid match {
          case Some(x) => anorm.Id(x)
          case None => NotAssigned
        }
        Gift(
            id=pkid,
            creator=User.findById(creatorid).get, 
            event=Event.findById(eventid).get, 
            name=name, 
            urls=urls
            )
      }
    },{ /*unapply*/
      gift: Gift => (
            gift.id.toOption,
            gift.creator.id.get,
            gift.event.id.get,
            gift.name,
            gift.urls)
    })
  }

  def newGift(eventid: Long) = IsAuthenticated { user => implicit request =>
    Ok(views.html.gifts.edit_gift(user, giftForm.fill(Gift(creator=user, event=Event.findById(eventid).get, name=""))))
  }
  def postEditGift() = IsAuthenticated { user => implicit request =>
    giftForm.bindFromRequest.fold(
      errors => {
        println(errors)
        BadRequest(views.html.gifts.edit_gift(user, errors))
      },
      gift => {
        gift.id.toOption match {
          case Some(x) => Gift.update(gift)
          case None => Gift.create(gift)
        }
        Redirect(routes.Events.event(gift.event.id.get)).withSession("userId" -> user.id.toString)
      }
    )
  }  
  
  def editGift(giftid: Long) = IsAuthenticated { user => implicit request =>
    Ok(views.html.gifts.edit_gift(user, giftForm.fill(Gift.findById(giftid).get)))
  }
  
  /**
   * Delete an event.
   */
  def postDeleteGift(giftid: Long) = IsCreatorOfGift(giftid) { user => implicit request =>
    Gift.findById(giftid) match {
      case Some(gift) => { 
        Gift.delete(giftid)
        Redirect(routes.Events.event(gift.event.id.get)).withSession("userId" -> user.id.toString)
      }
      case None => BadRequest
    }
  }
  
  
  def addParticipant() = IsAuthenticated { user => implicit request =>
    
    addParticipantForm.bindFromRequest.fold(
      errors => {
        println(errors)
        BadRequest(views.html.participants.participants_add_form(errors))
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
            val user = User.findByEmail(email) match {
              case Some(user) => user
              case None => {
                User.create(
                    User(name=email, isNotMember=true), 
                    Identity(email=email, adapter=Identity.Adapter.UserWithPassword)
                  )
              }
            }
            
            Participant.create(Participant(
              user=user,
              event=Event.findById(eventid).get,
              role=role))
          }
        }
        
        Ok(views.html.participants.participants_table(user, Participant.findByEventId(eventid)))
      }
    )
  }

}
