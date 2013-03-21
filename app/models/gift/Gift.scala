package models.gift

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
  event: Event,
  status: Gift.Status.Value = Gift.Status.New,
  creationDate: DateTime = DateTime.now)

object Gift {
  object Status extends Enumeration {
    val New = Value(1, "New")
    val AboutToBeBought = Value(2, "AboutToBeBought")
    val Bought = Value(3, "Bought")
    val MarkedForDeletion = Value(4, "MarkedForDeletion")
  }

  case class GiftContent(
    status: Int,
    creationDate: DateTime
    )
    
  implicit val GiftContentReads = Json.reads[GiftContent]
  implicit val GiftContentWrites = Json.writes[GiftContent]

  private case class BaseGift(
    id: Pk[Long] = NotAssigned,
    eventid: Long,
    content: String)
  private object BaseGift {
    val simple =
      get[Pk[Long]]("gift.id") ~
      get[Long]("gift.eventid") ~
      get[String]("gift.content") map {
        case id~eventid~content =>
          BaseGift(id, eventid, content)
    }    
    
    
    def create(gift: BaseGift) =
      DB.withConnection { implicit connection =>
        val id = gift.id.getOrElse{
            SQL("select next value for gift_seq").as(scalar[Long].single)
        }
        
        println("coneeent = " + gift.content)
        
        SQL(
        """
            insert into gift values (
              {id}, {eventid}, {content}
            )
        """    
        ).on(
          'id -> id,
          'eventid -> gift.eventid,
          'content -> gift.content
        ).executeUpdate()
        
        println("added " + id + " to event " + gift.eventid)
        
        gift.copy(id = Id(id))
      }
  }

  def create(gift: Gift): Gift =
    DB.withConnection { implicit connection =>
      // Participant creation done separately again to get the generated Id
      
      val baseGift = BaseGift.create(BaseGift(
          eventid = gift.event.id.get,
          content = GiftContentWrites.writes(GiftContent(gift.status.id, gift.creationDate)).toString()))
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
          Gift(
            id=g.id,
            event=Event.findById(g.eventid).get,
            status=Gift.Status(giftContent.status),
            creationDate = new DateTime(giftContent.creationDate)
            )
          }
        )
  }

}
