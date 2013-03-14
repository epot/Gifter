package models.gift

import java.util.Date
import models.user._
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import java.text.SimpleDateFormat
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat

case class Event(
  id: Pk[Long] = NotAssigned,
  creator: User,
  name: String,
  date: DateTime = DateTime.now
  ) {

  def isOwner(user: User) = creator == user
}

object Event {

  val simple =
    get[Pk[Long]]("event.id") ~
    get[Long]("event.creatorid") ~
    get[String]("event.name") ~
    get[Date]("event.date") map {
      case id~creatorid~name~date =>
        Event(id, User.findById(creatorid).get, name, new DateTime(date))
  }
  def create(event: Event): Event =
  DB.withConnection { implicit connection =>
      // Get the user id
      val id: Long = event.id.getOrElse {
        SQL("select next value for event_seq").as(scalar[Long].single)
      }
      
      SQL(
        """
          insert into event values (
            {id}, {creatorid}, {name}, {date}
          )
        """
      ).on(
        'id -> id,
        'creatorid -> event.creator.id,
        'name -> event.name,
        'date -> event.date.toDate
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
    .on('id -> user.id)
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
