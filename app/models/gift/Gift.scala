package models.gift

import java.util.Date
import java.util.UUID
import models.user._
import services.user._
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

import play.api.libs.json._
import play.api.libs.functional.syntax._

import org.joda.time.DateTime
import java.sql.Clob

import play.i18n.Messages

case class GiftSimple(
  id: Pk[Long] = NotAssigned,
  creatorid: UUID,
  event: Event,
  creationDate: DateTime = DateTime.now,
  name: String,
  status: Gift.Status.Value = Gift.Status.New,
  toid: Option[UUID] = None,
  fromid: Option[UUID] = None,
  urls: List[String]=Nil)

case class Gift(
  id: Pk[Long] = NotAssigned,
  creator: User,
  event: Event,
  creationDate: DateTime = DateTime.now,
  name: String,
  status: Gift.Status.Value = Gift.Status.New,
  to: Option[User] = None,
  from: Option[User] = None,
  urls: List[String]=Nil)

object Gift {
  object Status extends Enumeration {
    val New = Value(1, Messages.get("gift.statut.new"))
    val AboutToBeBought = Value(2, Messages.get("gift.statut.abouttobebought"))
    val Bought = Value(3, Messages.get("gift.statut.bought"))
    val MarkedForDeletion = Value(4, Messages.get("gift.statut.markedfordeletion"))
  }

  case class GiftContent(
    name: String,
    status: Int,
    to: Option[UUID],
    from: Option[UUID],
    urls: List[String]
    )
    
  implicit val GiftContentReads = Json.reads[GiftContent]
  implicit val GiftContentWrites = Json.writes[GiftContent]

  private case class BaseGift(
    id: Pk[Long] = NotAssigned,
    creatorid: UUID,
    eventid: Long,
    creationDate: DateTime,
    content: String)
  private object BaseGift {
    val simple =
      get[Pk[Long]]("gift.id") ~
      get[UUID]("gift.creator_id") ~
      get[Long]("gift.eventid") ~
      get[Date]("gift.creationDate") ~ 
      get[String]("gift.content") map {
        case id~creatorid~eventid~creationDate~content =>
          BaseGift(id, creatorid, eventid, new DateTime(creationDate), content)
    }    
    
    
    def create(gift: BaseGift) =
      DB.withConnection { implicit connection =>
        val id = gift.id.getOrElse{
            SQL("select nextval('gift_seq')").as(scalar[Long].single)
        }
        
        SQL(
        """
            insert into gift values (
              {id}, 2, {eventid}, {creationDate}, {content}, {creatorid}::uuid
            )
        """    
        ).on(
          'id -> id,
          'creatorid -> gift.creatorid,
          'eventid -> gift.eventid,
          'creationDate -> gift.creationDate.toDate,
          'content -> gift.content
        ).executeUpdate()
        
        gift.copy(id = Id(id))
      }
  }

  def create(gift: Gift): Gift =
    DB.withConnection { implicit connection =>
      // Participant creation done separately again to get the generated Id
      
      val toid = gift.to match {
        case Some(user) => Some(user.id)
        case None => None
      }
      
      val fromid = gift.from match {
        case Some(user) => Some(user.id)
        case None => None
      }
      
      val baseGift = BaseGift.create(BaseGift(
          creatorid = gift.creator.id,
          eventid = gift.event.id.get,
          creationDate = gift.creationDate,
          content = GiftContentWrites.writes(GiftContent(gift.name, gift.status.id, toid, fromid, gift.urls)).toString()))
      gift.copy(id = baseGift.id)
  }
  
  def update(gift: Gift): Gift = 
  DB.withConnection{ implicit connectin =>
      val toid = gift.to match {
        case Some(user) => Some(user.id)
        case None => None
      }
      
      val fromid = gift.from match {
        case Some(user) => Some(user.id)
        case None => None
      }

    SQL("""
        update gift set
        content={content}
        where id = {id}
        """).on(
        'id -> gift.id.get,
        'content -> GiftContentWrites.writes(GiftContent(gift.name, gift.status.id, toid, fromid, gift.urls)).toString()
      ).executeUpdate()
      
      
    Gift.findById(gift.id.get).get
  }

  
  /**
   * Delete a gift.
   */
  def delete(id: Long) {
    DB.withConnection { implicit connection => 
      SQL("delete from gift where id = {id}").on(
        'id -> id
      ).executeUpdate()
    }
  }
    
  def findById(id: Long): Option[Gift] = {
    find("id = " + id) match {
      case Nil => None
      case list => Some(list.head)
    }
  }
    

  def find(where: String = ""): List[Gift] =
  DB.withConnection { implicit connection =>
    val whereClause = where match {
      case s if (where != "") => "where "+s
      case _ => ""
    }
      
    SQL(
        """
         select id, creator_id, eventid, creationDate, content from gift 
        """+whereClause +
        """ order by creationDate"""
      ).as(BaseGift.simple *)
      .map(g => {
          val giftContent = GiftContentReads.reads(Json.parse(g.content)).get
          // ugly way to keep the way event class is designed with the new asynchronous user services
          
          val to = giftContent.to match {
            case Some(id) => UserSearchService.blocking_ugly_retrieve_option(id)
            case None => None
          }
          val from = giftContent.from match {
            case Some(id) => UserSearchService.blocking_ugly_retrieve_option(id)
            case None => None
          }
          
          Gift(
            id=g.id,
            creator=UserSearchService.blocking_ugly_retrieve(g.creatorid),
            event=Event.findById(g.eventid).get,
            name = giftContent.name,
            creationDate= g.creationDate,
            status=Gift.Status(giftContent.status),
            to=to,
            from=from,
            urls=giftContent.urls
            )
          }
        )
  }
  
  /**
   * @param eventid
   * @return list of gifts for the given event
   */
  def findByEventId(eventid: Long): List[Gift] =
    find("gift.eventid = " + eventid)
  
  /**
   * Check if a user is the creator of this task
   */
  def isCreator(giftid: Long, userid: UUID): Boolean = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select count(gift.id) = 1 from gift
          where gift.id = {giftid} and gift.creator_id = {creatorid}::uuid
        """
      ).on(
        'giftid -> giftid,
        'creatorid -> userid
      ).as(scalar[Boolean].single)
    }
  }


}
