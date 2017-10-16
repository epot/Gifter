package models.daos

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import models.gift.Event
import models.user.User
import play.api.db.slick.DatabaseConfigProvider
import com.github.nscala_time.time.Imports._

import scala.concurrent.{ExecutionContext, Future}


/**
 * Give access to the user object using Slick
 */
class EventDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                             implicit val ex: ExecutionContext) extends EventDAO with DAOSlick {

  import profile.api._

  def isCreator(eventid: Long, userid: UUID): Future[Boolean] = {

    val eventQuery = for {
      dbEvent <- slickEvents.filter(e => e.id === eventid && e.creatorId === userid)
    } yield dbEvent
    db.run(eventQuery.result.headOption).map { dbEvent =>
      dbEvent.isDefined
    }
  }

  /**
    * Finds a event by its ID.
    *
    * @return The found event or None if no user for the given ID could be found.
    */
  def find(eventId: Long) = {

    val eventQuery = for {
      dbEvent <- slickEvents.filter(_.id === eventId)
      dbUser <- slickUsers.filter(_.id === dbEvent.creatorId)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.userID === dbUser.id)
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
    } yield (dbEvent, dbUser, dbLoginInfo)
    db.run(eventQuery.result.headOption).map { resultOption =>
      resultOption.map { case(event, user, loginInfo) =>
        val creator = User(
          user.userID,
          List(LoginInfo(loginInfo.providerID, loginInfo.providerKey)),
          user.firstName,
          user.lastName,
          user.fullName,
          user.email,
          user.avatarURL)
        Event(event.id, creator, event.name, event.date, Event.Type.fromId(event.eventType))
      }
    }
  }

  def findByUser(user: User) = {
    val eventQuery = for {
      dbParticipant <- slickParticipants.filter(_.userId === user.id)
      dbEvent <- slickEvents.filter(e => e.id === dbParticipant.eventId || e.creatorId === user.id)
      dbUser <- slickUsers.filter(_.id === dbEvent.creatorId)
    } yield (dbEvent, dbUser)
    db.run(eventQuery.result).map { results =>
      (for(result <- results) yield {
        val creator = User(
          result._2.userID,
          Nil,
          result._2.firstName,
          result._2.lastName,
          result._2.fullName,
          result._2.email,
          result._2.avatarURL)
        val event = result._1
        Event(event.id, creator, event.name, event.date, Event.Type.fromId(event.eventType))
      }).distinct.toList.sortBy(_.date).reverse
    }
  }
  /**
    * Saves a user.
    *
    * @param event The event to save.
    * @return The saved event.
    */
  def save(event: Event) = {
    val dbEvent = DBEvent(
      event.id,
      event.creator.id,
      event.name,
      event.date,
      Event.Type.id(event.eventType))

    val insertQuery = (slickEvents returning slickEvents.map(_.id)).insertOrUpdate(dbEvent)
    dbConfig.db.run(insertQuery).map(id => event.copy(id=id))
  }

  def delete(id: Long) = {
    val q = slickEvents.filter(_.id === id)
    db.run(q.delete)
  }

}
