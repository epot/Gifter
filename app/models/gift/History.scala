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

case class History(
  id: Pk[Long] = NotAssigned,
  objectid: Long,
  user: User,
  creationDate: DateTime = DateTime.now,
  category: String,
  content: String)

object History {
  val simple =
    get[Pk[Long]]("history.id") ~
    get[Long]("history.objectid") ~ 
    get[Long]("history.userid") ~ 
    get[Date]("history.creationDate") ~ 
    get[String]("history.category")  ~ 
    get[String]("history.content") map {
      case id~objectid~userid~creationDate~category~content =>
        History(id, objectid, User.findById(userid).get, new DateTime(creationDate), category, content)
  }    
    
    
  def create(history: History) =
    DB.withConnection { implicit connection =>
      val id = history.id.getOrElse{
          SQL("select nextval('history_seq')").as(scalar[Long].single)
      }
      
      SQL(
      """
          insert into history values (
            {id}, {objectid}, {userid}, {creationDate}, {category}, {content}
          )
      """    
      ).on(
        'id -> id,
        'objectid -> history.objectid,
        'userid -> history.user.id.get,
        'creationDate -> history.creationDate.toDate,
        'category -> history.category,
        'content -> history.content
      ).executeUpdate()
      
      println("coinc coin " + history.objectid + ", " + history.category)
      
      history.copy(id = Id(id))
    }
   
  def findByCategoryAndId(category: String, objectid: Long): List[History] =
  DB.withConnection{ implicit connection =>
    SQL("""
      select * from history
      where category = {category} and objectid={objectid}
      order by creationdate
    """)
    .on('category -> category,
        'objectid -> objectid)
      .as(History.simple *)
  }


}
