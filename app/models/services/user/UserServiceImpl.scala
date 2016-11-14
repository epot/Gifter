package models.services.user

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import jdub.async.Database
import models.queries.UserQueries
import models.user.User
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
 * Handles actions to users.
 */
class UserServiceImpl @Inject() extends UserService {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: UUID) = Database.query(UserQueries.getById(Seq(id)))

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = if (loginInfo.providerID == "anonymous") {
    Database.query(UserQueries.getById(Seq(UUID.fromString(loginInfo.providerKey)))).map {
      case Some(dbUser) =>
        if (dbUser.profiles.nonEmpty) {
          Logger.warn(s"Attempt to authenticate as anonymous for user with profiles [${dbUser.profiles}].")
          None
        } else {
          Some(dbUser)
        }
      case None => None
    }
  } else {
    Database.query(UserQueries.FindUserByProfile(loginInfo))
  }

  def blocking_ugly_retrieve(id: UUID) = {
    val user = retrieve(id)
    Await.result(user, Duration.Inf)
    user.value.get.toOption.get.get
  }
  def blocking_ugly_retrieve_option(id: UUID) = {
    val user = retrieve(id)
    Await.result(user, Duration.Inf)
    user.value.get.toOption.get
  }

  def save(user: User) = save(user, false)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User, update: Boolean = false) = {
    val statement = if (update) {
      Logger.info(s"Updating user [$user].")
      UserQueries.UpdateUser(user)
    } else {
      Logger.info(s"Creating new user [$user].")
      UserQueries.insert(user)
    }
    Database.execute(statement).map { i =>
      user
    }
  }

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile) = {
    UserSearchService.retrieve(profile.loginInfo).flatMap {
      case Some(user) => // Update user with profile
        save(user.copy(
          username = if (profile.firstName.isDefined && user.username.isEmpty) {
              profile.firstName
            } else {
              user.username },
          profiles = user.profiles.filterNot(_.providerID == profile.loginInfo.providerID) :+ profile.loginInfo
        ), true)
      case None => // Insert a new user
        save(User(
          id = UUID.randomUUID(),
          username = profile.firstName,
          profiles = List(profile.loginInfo)
        ))
    }
  }
}
