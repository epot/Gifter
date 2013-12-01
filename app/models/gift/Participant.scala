package models.gift

import models.user._
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import play.i18n.Messages

case class Participant(
  id: Pk[Long] = NotAssigned,
  user: User,
  event: Event,
  role: Participant.Role.Value)

object Participant {
  object Role extends Enumeration {
    val Owner = Value(1, Messages.get("participant.statut.owner"))
    val Gifter = Value(2, Messages.get("participant.statut.gifter"))
    val Reader = Value(3, Messages.get("participant.statut.reader"))
    class ParticipantVal(name: String, val x : String) extends Val(nextId, name)
    protected final def Value(name: String, x : String): ParticipantVal = new ParticipantVal(name, x)
  }

  private case class BaseParticipant(
    id: Pk[Long] = NotAssigned,
    userid: Long,
    eventid: Long,
    role: Int)
  private object BaseParticipant {
    val simple =
      get[Pk[Long]]("participant.id") ~
      get[Long]("participant.userid") ~
      get[Long]("participant.eventid") ~
      get[Int]("participant.participant_role") map {
        case id~userid~eventid~role =>
          BaseParticipant(id, userid, eventid, role)
    }    
    
    
    def create(participant: BaseParticipant) =
      DB.withConnection { implicit connection =>
        val id = participant.id.getOrElse{
            SQL("select nextval('participant_seq')").as(scalar[Long].single)
        }
        
        SQL(
        """
            insert into participant values (
              {id}, {userid}, {eventid}, {role}
            )
        """    
        ).on(
          'id -> id,
          'userid -> participant.userid,
          'eventid -> participant.eventid,
          'role -> participant.role
        ).executeUpdate()
        
        println("added " + id + " to event " + participant.eventid)
        
        participant.copy(id = Id(id))
      }
  }

  def create(participant: Participant): Participant =
    DB.withConnection { implicit connection =>
      // Participant creation done separately again to get the generated Id
      val baseParticipant = BaseParticipant.create(BaseParticipant(
          eventid = participant.event.id.get, 
          userid = participant.user.id.get,
          role = participant.role.id))
      participant.copy(id = baseParticipant.id)
    }

  def update(id: Long, role: Participant.Role.Value) =
    DB.withConnection { implicit connection =>
        SQL(
        """
            update participant set 
              participant_role = {role}
              where id={id}
        """    
        ).on(
          'id -> id,
          'role -> role.id
        ).executeUpdate()
    }

  /**
   * @param eventid
   * @return list of participants for the given event
   */
  def findByEventId(eventid: Long): List[Participant] =
    DB.withConnection { implicit connection =>

      SQL(
        """
         select * from participant 
         where eventid = {eventid}
      """
      ).onParams(eventid).as(BaseParticipant.simple *)
      .map(part => Participant(
          id=part.id,
          user=User.findById(part.userid).get,
          event=Event.findById(part.eventid).get,
          role=Participant.Role(part.role)))
  }
  
  /**
   * @param evenit
   * @return a participant if given user participates to this event
   */
  def findByEventIdAndByEmail(eventid: Long, email: String): Option[Participant] =
    DB.withConnection { implicit connection =>

      SQL(
        """
         select * from participant
         join identity on identity.userid = participant.userid
         where participant.eventid = {eventid} 
           and identity.email = {email}
      """
      ).on(
          'eventid -> eventid,
          'email -> email)
      .as(BaseParticipant.simple.singleOpt) match {
        case Some(part) => {
          Some(Participant(
            id=part.id,
            user=User.findById(part.userid).get,
            event=Event.findById(part.eventid).get,
            role=Participant.Role(part.role)))
        }
        case None => None
      }
  }
  
  /**
   * @param eventid
   * @param userid
   * @return a participant if given user participates to this event
   */
  def findByEventIdAndByUserId(eventid: Long, userid: Long): Option[Participant] =
    DB.withConnection { implicit connection =>

      SQL(
        """
         select * from participant
         where eventid = {eventid} 
           and userid = {userid}
      """
      ).on(
          'eventid -> eventid,
          'userid ->  userid)
      .as(BaseParticipant.simple.singleOpt) match {
        case Some(part) => {
          Some(Participant(
            id=part.id,
            user=User.findById(part.userid).get,
            event=Event.findById(part.eventid).get,
            role=Participant.Role(part.role)))
        }
        case None => None
      }
  }

}
