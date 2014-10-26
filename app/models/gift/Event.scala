package models.gift

import java.util.Date
import models.user._
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import play.i18n.Messages
import java.text.SimpleDateFormat
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat

case class Event(
  id: Pk[Long] = NotAssigned,
  creator: User,
  name: String,
  date: DateTime,
  eventtype: Event.Type.Value
  ) {

  def isOwner(user: User) = creator == user
}

object Event {
  object Type extends Enumeration {
    val Birthday = Value(1, Messages.get("event.type.birthday"))
    val Christmas = Value(2, Messages.get("event.type.christmas"))
  }

  val simple =
    get[Pk[Long]]("event.id") ~
    get[Long]("event.creatorid") ~
    get[String]("event.name") ~
    get[Date]("event.date") ~ 
    get[Int]("event.type") map {
      case id~creatorid~name~date~eventtype =>
        Event(id, User.findById(creatorid).get, name, new DateTime(date), Type(eventtype))
  }
  def create(event: Event): Event =
  DB.withConnection { implicit connection =>
      // Get the user id
      val id: Long = event.id.getOrElse {
        SQL("select nextval('event_seq')").as(scalar[Long].single)
      }
      
      SQL(
        """
          insert into event values (
            {id}, {creatorid}, {name}, {date}, {eventtype}
          )
        """
      ).on(
        'id -> id,
        'creatorid -> event.creator.id.get,
        'name -> event.name,
        'date -> event.date.toDate,
        'eventtype -> event.eventtype.id
      ).executeUpdate()
      
      event.copy(id = Id(id))
  }

  /**
   * Delete an event.
   */
  def delete(id: Long) {
    DB.withConnection { implicit connection => 
      SQL("delete from event where id = {id}").on(
        'id -> id
      ).executeUpdate()
    }
  }
    
  def findById(id: Long): Option[Event] =
  DB.withConnection{ implicit connection =>
    SQL("select * from event where id = {id}")
      .on('id -> id)
      .as(Event.simple.singleOpt)
  }

  /**
   * Find all events related to a user
   */
  def findByUser(user: User): List[Event] =
  DB.withConnection{ implicit connection =>
    SQL("""
      select distinct event.* from event
      left outer join participant on participant.eventid = event.id
      where creatorid = {id} or participant.userid={id}
    """)
    .on('id -> user.id.get)
      .as(Event.simple *)
  }
  
  /**
   * Check if a user is the creator of this task
   */
  def isCreator(eventid: Long, userid: Long): Boolean = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select count(event.id) = 1 from event
          where event.id = {eventid} and event.creatorid = {creatorid}
        """
      ).on(
        'eventid -> eventid,
        'creatorid -> userid
      ).as(scalar[Boolean].single)
    }
  }

}
