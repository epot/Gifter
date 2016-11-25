package models.daos

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import models.user.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Give access to the user object using Slick
 */
class UserDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends UserDAO with DAOSlick {

  import driver.api._

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo) = {
    val userQuery = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.loginInfoId === dbLoginInfo.id)
      dbUser <- slickUsers.filter(_.id === dbUserLoginInfo.userID)
    } yield (dbUser)
    db.run(userQuery.result.headOption).map { dbUserOption =>
      dbUserOption.map { user =>
        User(user.userID, List(loginInfo), user.firstName, user.lastName, user.fullName, user.email, user.avatarURL)
      }
    }
  }

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: UUID) = {
    val query = for {
      dbUser <- slickUsers.filter(_.id === userID)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.userID === dbUser.id)
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
    } yield (dbUser, dbLoginInfo)
    db.run(query.result.headOption).map { resultOption =>
      resultOption.map {
        case (user, loginInfo) =>
          User(
            user.userID,
            List(LoginInfo(loginInfo.providerID, loginInfo.providerKey)),
            user.firstName,
            user.lastName,
            user.fullName,
            user.email,
            user.avatarURL)
      }
    }
  }
  def findByEmail(email: String) = {
    val userQuery = for {
      dbUser <- slickUsers.filter(_.email === email)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.userID === dbUser.id)
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
    } yield (dbUser, dbLoginInfo)
    db.run(userQuery.result.headOption).map { resultOption =>
      resultOption.map { case (user, loginInfo) =>
        User(user.userID,
          List(LoginInfo(loginInfo.providerID, loginInfo.providerKey)),
          user.firstName,
          user.lastName,
          user.fullName,
          user.email,
          user.avatarURL)
      }
    }
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = {
    val dbUser = DBUser(user.id, user.firstName, user.lastName, user.fullName, user.email, user.avatarURL)

    // We don't have the LoginInfo id so we try to get it first.
    // If there is no LoginInfo yet for this user we retrieve the id on insertion.    
    val loginInfoActions = for(loginInfo <- user.profiles) yield {
      val dbLoginInfo = DBLoginInfo(None, loginInfo.providerID, loginInfo.providerKey)
      val retrieveLoginInfo = slickLoginInfos.filter(
        info => info.providerID === loginInfo.providerID &&
          info.providerKey === loginInfo.providerKey).result.headOption
      val insertLoginInfo = slickLoginInfos.returning(slickLoginInfos.map(_.id)).
        into((info, id) => info.copy(id = Some(id))) += dbLoginInfo
      for {
        loginInfoOption <- retrieveLoginInfo
        loginInfo <- loginInfoOption.map(DBIO.successful(_)).getOrElse(insertLoginInfo)
      } yield loginInfo
    }

    // combine database actions to be run sequentially
    val actions = (for {
      _ <- slickUsers.insertOrUpdate(dbUser)
      loginInfoActionList <- DBIO.sequence(loginInfoActions)
      _ <- DBIO.sequence { loginInfoActionList.map { loginInfo =>
        slickUserLoginInfos += DBUserLoginInfo(dbUser.userID, loginInfo.id.get) }
      }
    } yield ()).transactionally
    // run actions and return user afterwards
    db.run(actions).map(_ => user)
  }
}
