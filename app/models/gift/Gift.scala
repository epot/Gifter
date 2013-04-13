package models.gift

import java.util.Date
import models.user._
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

import play.api.libs.json._
import play.api.libs.functional.syntax._

import org.joda.time.DateTime
import java.sql.Clob

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
    val New = Value(1, "New")
    val AboutToBeBought = Value(2, "AboutToBeBought")
    val Bought = Value(3, "Bought")
    val MarkedForDeletion = Value(4, "MarkedForDeletion")
  }

  case class GiftContent(
    name: String,
    status: Int,
    to: Option[Long],
    from: Option[Long],
    urls: List[String]
    )
    
  implicit val GiftContentReads = Json.reads[GiftContent]
  implicit val GiftContentWrites = Json.writes[GiftContent]

  private case class BaseGift(
    id: Pk[Long] = NotAssigned,
    creatorid: Long,
    eventid: Long,
    creationDate: DateTime,
    content: String)
  private object BaseGift {
    val simple =
      get[Pk[Long]]("gift.id") ~
      get[Long]("gift.creatorid") ~
      get[Long]("gift.eventid") ~
      get[Date]("gift.creationDate") ~ 
      get[String]("gift.content") map {
        case id~creatorid~eventid~creationDate~content =>
          BaseGift(id, creatorid, eventid, new DateTime(creationDate), content)
    }    
    
    
    def create(gift: BaseGift) =
      DB.withConnection { implicit connection =>
        val id = gift.id.getOrElse{
            SQL("select next value for gift_seq").as(scalar[Long].single)
        }
        
        SQL(
        """
            insert into gift values (
              {id}, {creatorid}, {eventid}, {creationDate}, {content}
            )
        """    
        ).on(
          'id -> id,
          'creatorid -> gift.creatorid,
          'eventid -> gift.eventid,
          'creationDate -> gift.creationDate.toDate,
          'content -> gift.content
        ).executeUpdate()
        
        println("added " + id + " to event " + gift.eventid)
        
        gift.copy(id = Id(id))
      }
  }

  def create(gift: Gift): Gift =
    DB.withConnection { implicit connection =>
      // Participant creation done separately again to get the generated Id
      
      val toid = gift.to match {
        case Some(user) => Some(user.id.get)
        case None => None
      }
      
      val fromid = gift.from match {
        case Some(user) => Some(user.id.get)
        case None => None
      }
      
      val baseGift = BaseGift.create(BaseGift(
          creatorid = gift.creator.id.get,
          eventid = gift.event.id.get,
          creationDate = gift.creationDate,
          content = GiftContentWrites.writes(GiftContent(gift.name, gift.status.id, toid, fromid, gift.urls)).toString()))
      gift.copy(id = baseGift.id)
  }
  
  /**
   * @param eventid
   * @return list of gifts for the given event
   */
  def findByEventId(eventid: Long): List[Gift] =
    DB.withConnection { implicit connection =>

      SQL(
        """
         select * from gift 
         where eventid = {eventid}
      """
      ).onParams(eventid).as(BaseGift.simple *)
      .map(g => {
          val giftContent = GiftContentReads.reads(Json.parse(g.content)).get
          val to = giftContent.to match {
            case Some(id) => User.findById(id)
            case None => None
          }
          val from = giftContent.from match {
            case Some(id) => User.findById(id)
            case None => None
          }
          Gift(
            id=g.id,
            creator=User.findById(g.creatorid).get,
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

}
