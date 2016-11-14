package models.gift

import java.util.UUID
import models.user._
import models.services.user._
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import play.i18n.Messages

case class Participant(
  id: Option[Long] = None,
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
    id: Option[Long] = None,
    userid: UUID,
    eventid: Long,
    role: Int)
  private object BaseParticipant {
    val simple =
      get[Option[Long]]("participant.id") ~
      get[UUID]("participant.user_id") ~
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
              {id}, 1, {eventid}, {role}, {userid}::uuid
            )
        """    
        ).on(
          'id -> id,
          'userid -> participant.userid,
          'eventid -> participant.eventid,
          'role -> participant.role
        ).executeUpdate()
        
        participant.copy(id = Some(id))
      }
  }

  def create(participant: Participant): Participant =
    DB.withConnection { implicit connection =>
      // Participant creation done separately again to get the generated Id
      val baseParticipant = BaseParticipant.create(BaseParticipant(
          eventid = participant.event.id.get, 
          userid = participant.user.id,
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
         select id, user_id, eventid, participant_role from participant 
         where eventid = {eventid}
      """
      ).onParams(eventid).as(BaseParticipant.simple *)
      .map(part => Participant(
          id=part.id,
          user=UserSearchService.blocking_ugly_retrieve(part.userid),
          event=Event.findById(part.eventid).get,
          role=Participant.Role(part.role)))
  }
  
  /**
   * @param eventid
   * @param userid
   * @return a participant if given user participates to this event
   */
  def findByEventIdAndByUserId(eventid: Long, userid: UUID): Option[Participant] =
    DB.withConnection { implicit connection =>

      SQL(
        """
         select id, user_id, eventid, participant_role from participant
         where eventid = {eventid} 
           and user_id = {userid}::uuid
      """
      ).on(
          'eventid -> eventid,
          'userid ->  userid)
      .as(BaseParticipant.simple.singleOpt) match {
        case Some(part) => {  
          Some(Participant(
            id=part.id,
            user=UserSearchService.blocking_ugly_retrieve(part.userid),
            event=Event.findById(part.eventid).get,
            role=Participant.Role(part.role)))
        }
        case None => None
      }
  }

}
