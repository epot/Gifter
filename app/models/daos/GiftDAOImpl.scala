package models.daos

import java.util.UUID
import javax.inject.Inject

import models.gift.{Gift, Notification}
import models.gift.Gift._
import models.JsonFormat._
import models.user.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}

/**
 * Give access to the user object using Slick
 */
class GiftDAOImpl @Inject()(
   notificationDAO: NotificationDAO,
   protected val dbConfigProvider: DatabaseConfigProvider,
   implicit val ex: ExecutionContext) extends GiftDAO with DAOSlick {

  import profile.api._

  def isCreator(giftId: Long, userid: UUID): Future[Boolean] = {
    val eventQuery = for {
      dbGift <- slickGifts.filter(g => g.id === giftId && g.creatorId === userid)
    } yield dbGift
    db.run(eventQuery.result.headOption).map { dbGift =>
      dbGift match {
        case Some(_) => true
        case None => false
      }
    }
  }

  def getUser(id: UUID): DBIO[Option[DBUser]] = {
    slickUsers.filter(_.id === id).result.headOption
  }


  /**
    * Finds a event by its ID.
    *
    * @return The found event or None if no user for the given ID could be found.
    */
  def find(giftId: Long) = {

    val giftQuery = for {
      dbGift <- slickGifts.filter(_.id === giftId)
      dbUser <- slickUsers.filter(_.id === dbGift.creatorId)
    } yield (dbGift, dbUser)
    db.run(giftQuery.result.headOption).flatMap { resultOption =>
      resultOption match {
        case Some((gift, user)) => {
          val creator = User(
            user.userID,
            Nil,
            user.firstName,
            user.lastName,
            user.fullName,
            user.email,
            user.avatarURL)

          val g = BaseGift(gift.id, creator, gift.eventId, gift.creationDate, gift.content)

          val giftContent = Json.parse(g.content).as[GiftContent]

          val usersId = List(giftContent.to, giftContent.from).flatten
          val actions = DBIO.sequence(usersId.map(getUser))
          db.run(actions).map { optDBUsers =>
            val users = for (user <- optDBUsers.flatten) yield {
              User(user.userID, Nil, user.firstName, user.lastName, user.fullName, user.email, user.avatarURL)
            }

            val to = giftContent.to match {
              case Some(id) => users.filter(_.id == id).headOption
              case None => None
            }

            val from = giftContent.from match {
              case Some(id) => users.filter(_.id == id).headOption
              case None => None
            }
            Some(Gift(
              id = g.id,
              creator = creator,
              eventid = g.eventid,
              name = giftContent.name,
              creationDate = g.creationDate,
              status = Gift.Status.fromId(giftContent.status),
              to = to,
              from = from,
              urls = giftContent.urls,
              secret = giftContent.secret.getOrElse(false),
            ))
          }
        }
        case _ => Future.successful(None)
      }
    }
  }

  // http://stackoverflow.com/questions/20874186/scala-listfuture-to-futurelist-disregarding-failed-futures
  def allSuccessful[A, M[X] <: TraversableOnce[X]](in: M[Future[A]])
                                                  (implicit cbf: CanBuildFrom[M[Future[A]], A, M[A]]
                                                  ): Future[M[A]] = {
    in.foldLeft(Future.successful(cbf(in))) {
      (fr, fa) ⇒ (for (r ← fr; a ← fa) yield r += a) fallbackTo fr
    } map (_.result())
  }

  def findByEventId(userConnected: User, eventId: Long) = {

    val giftQuery = for {
      dbGift <- slickGifts.filter(_.eventId === eventId)
      dbUser <- slickUsers.filter(_.id === dbGift.creatorId)
    } yield (dbGift, dbUser)
    db.run(giftQuery.result).flatMap { results =>
      val listOfFutures = results.map {
        case (gift, user) => {
          val creator = User(
            user.userID,
            Nil,
            user.firstName,
            user.lastName,
            user.fullName,
            user.email,
            user.avatarURL)

          val g = BaseGift(gift.id, creator, gift.eventId, gift.creationDate, gift.content)

          val giftContent = Json.parse(g.content).as[GiftContent]

          val usersId = List(giftContent.to, giftContent.from).flatten
          val actions = DBIO.sequence(usersId.map(getUser))
          db.run(actions).flatMap { optDBUsers =>
            val users = for (user <- optDBUsers.flatten) yield {
              User(user.userID, Nil, user.firstName, user.lastName, user.fullName, user.email, user.avatarURL)
            }

            val to = giftContent.to match {
              case Some(id) => users.filter(_.id == id).headOption
              case None => None
            }

            val from = giftContent.from match {
              case Some(id) => users.filter(_.id == id).headOption
              case None => None
            }
            val giftModel = Gift(
              id = g.id,
              creator = creator,
              eventid = g.eventid,
              name = giftContent.name,
              creationDate = g.creationDate,
              status = Gift.Status.fromId(giftContent.status),
              to = to,
              from = from,
              urls = giftContent.urls,
              secret = giftContent.secret.getOrElse(false)
            )

            notificationDAO.hasNotification(userConnected, Notification.Category.GiftComment, gift.id.get).map { value =>
              GiftWithNotification(giftModel, value)
            }
          }
        }
      }.distinct.toList

      allSuccessful(listOfFutures)
    }
  }

  /**
    * Saves a Gift.
    *
    * @param gift The gift to save.
    * @return The saved gift.
    */
  def save(gift: Gift) = {
    val toid = gift.to.collect{case(user) => user.id}
    val fromid = gift.from.collect{case(user) => user.id}

    val dbGift = DBGift(
      gift.id,
      gift.creator.id,
      gift.eventid,
      gift.creationDate,
      Json.toJson(GiftContent(gift.name, Gift.Status.id(gift.status), toid, fromid, gift.urls, Some(gift.secret))).toString)

    val insertQuery = (slickGifts returning slickGifts.map(_.id)).insertOrUpdate(dbGift)
    dbConfig.db.run(insertQuery).map(_ => gift)
  }

  def delete(id: Long) = {
    val q = slickGifts.filter(_.id === id)
    db.run(q.delete)
  }

}
