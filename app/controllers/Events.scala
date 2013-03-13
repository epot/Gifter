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
        println("creating event: " + event)
        Event.create(event)
        Ok(views.html.userHome(user))
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
        Ok(views.html.userHome(user))
      }
      case None => BadRequest
    }
  }

}
