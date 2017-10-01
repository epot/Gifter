package models.daos

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject

import models.AuthToken
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext

/**
 * Give access to the [[AuthToken]] object.
 */


class AuthTokenDAOImpl @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider,
  implicit val ex: ExecutionContext) extends AuthTokenDAO with DAOSlick {

  import profile.api._

  implicit def jodaTimeMapping: BaseColumnType[DateTime] = MappedColumnType.base[DateTime, Timestamp] (
    dateTime => new Timestamp(dateTime.getMillis),
    timeStamp => new DateTime(timeStamp.getTime)
  )

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID) = {
    db.run(slickAuthToken.filter(_.id === id).result.headOption)
  }

  /**
   * Finds expired tokens.
   *
   * @param dateTime The current date time.
   */
  def findExpired(dateTime: DateTime) = {
    db.run(slickAuthToken.filter(_.expiry < dateTime).result)
  }

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken) = {
    dbConfig.db.run(slickAuthToken.insertOrUpdate(token)).map(_ => token)
  }

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID) = {
    db.run(slickAuthToken.filter(_.id === id).delete).map(_ => ())
  }
}