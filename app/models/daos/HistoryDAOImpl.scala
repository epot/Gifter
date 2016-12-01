package models.daos

import javax.inject.Inject

import models.gift.History
import models.user.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Give access to the user object using Slick
  */
class HistoryDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HistoryDAO with DAOSlick {

  import driver.api._

  def findByCategoryAndId(category: String, objectid: Long) = {
    val giftQuery = for {
      dbHistory <- slickHistory.filter(h => h.category === category && h.objectId === objectid)
      dbUser <- slickUsers.filter(_.id === dbHistory.userId)
    } yield (dbHistory, dbUser)
    db.run(giftQuery.result).map { results =>
      (for (result <- results) yield {
        val h = result._1
        val user = result._2
        val userObj = User(
          user.userID,
          Nil,
          user.firstName,
          user.lastName,
          user.fullName,
          user.email,
          user.avatarURL)

        History(h.id, h.objectId, userObj, h.creationDate, h.category, h.content)
      }).toList
    }
  }

  def save(history: History) = {
    val dbHistory = DBHistory(
      history.id,
      history.user.id,
      history.objectid,
      history.creationDate,
      history.category,
      history.content)

    val insertQuery = (slickHistory returning slickHistory.map(_.id)).insertOrUpdate(dbHistory)
    dbConfig.db.run(insertQuery).map(id => history.copy(id=id))
  }
}
