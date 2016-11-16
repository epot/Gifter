package models.gift

import java.util.Date
import java.util.UUID

import models.user._
import models.services.user._
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.i18n.Messages
import com.github.nscala_time.time.Imports._

import org.joda.time.DateTime
import play.api.Play.current


case class EventSimple(
  name: String,
  date: DateTime,
  eventtype: Event.Type.Value
)

case class Event(
  id: Option[Long] = None,
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
    get[Option[Long]]("event.id") ~
    get[UUID]("event.creator_id") ~
    get[String]("event.name") ~
    get[Date]("event.date") ~ 
    get[Int]("event.type") map {
      case id~creatorid~name~date~eventtype =>
        Event(id, UserSearchService.blocking_ugly_retrieve(creatorid), name, new DateTime(date), Type(eventtype))
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
            {id}, 3, {name}, {date}, {eventtype}, {creatorid}::uuid
          )
        """
      ).on(
        'id -> id,
        'creatorid -> event.creator.id,
        'name -> event.name,
        'date -> event.date.toDate,
        'eventtype -> event.eventtype.id
      ).executeUpdate()
      
      event.copy(id = Some(id))
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
    SQL("select id, creator_id, name, date, type from event where id = {id}")
      .on('id -> id)
      .as(Event.simple.singleOpt)
  }

  /**
   * Find all events related to a user
   */
  def findByUser(user: User): List[Event] =
  DB.withConnection{ implicit connection =>
    SQL("""
      select distinct event.id, event.creator_id, event.name, event.date, event.type from event
      left outer join participant on participant.eventid = event.id
      where creator_id = {id}::uuid or participant.user_id={id}::uuid
    """)
    .on('id -> user.id)
      .as(Event.simple *).sortBy(_.date).reverse
  }
  
  /**
   * Check if a user is the creator of this task
   */
  def isCreator(eventid: Long, userid: UUID): Boolean = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select count(event.id) = 1 from event
          where event.id = {eventid} and event.creator_id = {creatorid}::uuid
        """
      ).on(
        'eventid -> eventid,
        'creatorid -> userid
      ).as(scalar[Boolean].single)
    }
  }

}
