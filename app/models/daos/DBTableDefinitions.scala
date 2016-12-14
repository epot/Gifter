package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf
import com.github.tototoshi.slick.PostgresJodaSupport._
import models.AuthToken


trait DBTableDefinitions {
  
  protected val driver: JdbcProfile
  import driver.api._

  case class DBUser (
    userID: UUID,
    firstName: Option[String],
    lastName: Option[String],
    fullName: Option[String],
    email: Option[String],
    avatarURL: Option[String],
    created: DateTime=DateTime.now()
  )

  class Users(tag: Tag) extends Table[DBUser](tag, "user_profiles") {
    def id = column[UUID]("user_id", O.PrimaryKey)
    def firstName = column[Option[String]]("first_name")
    def lastName = column[Option[String]]("last_name")
    def fullName = column[Option[String]]("full_name")
    def email = column[Option[String]]("email")
    def avatarURL = column[Option[String]]("avatar_url")
    def created = column[DateTime]("created")
    def * = (id, firstName, lastName, fullName, email, avatarURL, created) <> (DBUser.tupled, DBUser.unapply)
  }

  case class DBLoginInfo (
    id: Option[Long],
    providerID: String,
    providerKey: String
  )

  class LoginInfos(tag: Tag) extends Table[DBLoginInfo](tag, "login_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def providerID = column[String]("providerID")
    def providerKey = column[String]("providerKey")
    def * = (id.?, providerID, providerKey) <> (DBLoginInfo.tupled, DBLoginInfo.unapply)
  }

  case class DBUserLoginInfo (
    userID: UUID,
    loginInfoId: Long
  )

  class UserLoginInfos(tag: Tag) extends Table[DBUserLoginInfo](tag, "user_login_info") {
    def userID = column[UUID]("userID")
    def loginInfoId = column[Long]("loginInfoId")
    def * = (userID, loginInfoId) <> (DBUserLoginInfo.tupled, DBUserLoginInfo.unapply)
  }

  case class DBPasswordInfo (
    hasher: String,
    password: String,
    salt: Option[String],
    loginInfoId: Long
  )

  class PasswordInfos(tag: Tag) extends Table[DBPasswordInfo](tag, "password_info") {
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[Option[String]]("salt")
    def loginInfoId = column[Long]("loginInfoId")
    def * = (hasher, password, salt, loginInfoId) <> (DBPasswordInfo.tupled, DBPasswordInfo.unapply)
  }

  case class DBOAuth1Info (
    id: Option[Long],
    token: String,
    secret: String,
    loginInfoId: Long
  )

  class OAuth1Infos(tag: Tag) extends Table[DBOAuth1Info](tag, "oauth1_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def token = column[String]("token")
    def secret = column[String]("secret")
    def loginInfoId = column[Long]("loginInfoId")
    def * = (id.?, token, secret, loginInfoId) <> (DBOAuth1Info.tupled, DBOAuth1Info.unapply)
  }

  case class DBOAuth2Info (
    id: Option[Long],
    accessToken: String,
    tokenType: Option[String],
    expiresIn: Option[Int],
    refreshToken: Option[String],
    loginInfoId: Long
  )

  class OAuth2Infos(tag: Tag) extends Table[DBOAuth2Info](tag, "oauth2_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def accessToken = column[String]("access_token")
    def tokenType = column[Option[String]]("token_type")
    def expiresIn = column[Option[Int]]("expires_in")
    def refreshToken = column[Option[String]]("refresh_token")
    def loginInfoId = column[Long]("loginInfoId")
    def * = (id.?, accessToken, tokenType, expiresIn, refreshToken, loginInfoId) <> (DBOAuth2Info.tupled, DBOAuth2Info.unapply)
  }

  case class DBEvent (
    id: Option[Long],
    creatorId: UUID,
    name: String,
    date: DateTime,
    eventType: Int
  )

  class Events(tag: Tag) extends Table[DBEvent](tag, "event") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def creatorId = column[UUID]("creator_id")
    def name = column[String]("name")
    def date = column[DateTime]("date")
    def eventType = column[Int]("type")
    def * = (id.?, creatorId, name, date, eventType) <> (DBEvent.tupled, DBEvent.unapply)
  }

  case class DBGift (
    id: Option[Long],
    creatorId: UUID,
    eventId: Long,
    creationDate: DateTime,
    content: String
  )

  class Gifts(tag: Tag) extends Table[DBGift](tag, "gift") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def creatorId = column[UUID]("creator_id")
    def eventId = column[Long]("eventid")
    def creationDate = column[DateTime]("creationdate")
    def content = column[String]("content")
    def * = (id.?, creatorId, eventId, creationDate, content) <> (DBGift.tupled, DBGift.unapply)
  }

  case class DBParticipant (
    id: Option[Long],
    userId: UUID,
    eventId: Long,
    participantRole: Int
  )

  class Participants(tag: Tag) extends Table[DBParticipant](tag, "participant") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[UUID]("user_id")
    def eventId = column[Long]("eventid")
    def participantRole = column[Int]("participant_role")
    def * = (id.?, userId, eventId, participantRole) <> (DBParticipant.tupled, DBParticipant.unapply)
  }

  case class DBHistory (
     id: Option[Long],
     userId: UUID,
     objectId: Long,
     creationDate: DateTime,
     category: String,
     content:String
   )

  class HistoryT(tag: Tag) extends Table[DBHistory](tag, "history") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[UUID]("user_id")
    def objectId = column[Long]("objectid")
    def creationDate = column[DateTime]("creationdate")
    def category = column[String]("category")
    def content = column[String]("content")
    def * = (id.?, userId, objectId, creationDate, category, content) <> (DBHistory.tupled, DBHistory.unapply)
  }

  case class DBComment (
     id: Option[Long],
     userId: UUID,
     objectId: Long,
     creationDate: DateTime,
     category: Int,
     content:String
   )

  class Comments(tag: Tag) extends Table[DBComment](tag, "comment") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[UUID]("userid")
    def objectId = column[Long]("objectid")
    def creationDate = column[DateTime]("creationdate")
    def category = column[Int]("category")
    def content = column[String]("content")
    def * = (id.?, userId, objectId, creationDate, category, content) <> (DBComment.tupled, DBComment.unapply)
  }

  case class DBNotification (
     id: Option[Long],
     userId: UUID,
     objectId: Long,
     category: Int
   )

  class Notifications(tag: Tag) extends Table[DBNotification](tag, "notification") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[UUID]("userid")
    def objectId = column[Long]("objectid")
    def category = column[Int]("category")
    def * = (id.?, userId, objectId, category) <> (DBNotification.tupled, DBNotification.unapply)
  }

  case class DBOpenIDInfo (
    id: String,
    loginInfoId: Long
  )
  
  class OpenIDInfos(tag: Tag) extends Table[DBOpenIDInfo](tag, "openid_info") {
    def id = column[String]("id", O.PrimaryKey)
    def loginInfoId = column[Long]("loginInfoId")
    def * = (id, loginInfoId) <> (DBOpenIDInfo.tupled, DBOpenIDInfo.unapply)
  }
  
  case class DBOpenIDAttribute (
    id: String,
    key: String,
    value: String
  )
  
  class OpenIDAttributes(tag: Tag) extends Table[DBOpenIDAttribute](tag, "openid_attributes") {
    def id = column[String]("id")
    def key = column[String]("key")
    def value = column[String]("value")
    def * = (id, key, value) <> (DBOpenIDAttribute.tupled, DBOpenIDAttribute.unapply)
  }

  class AuthTokens(tag: Tag) extends Table[AuthToken](tag, "auth_token") {
    def id = column[UUID]("id", O.PrimaryKey)
    def userId = column[UUID]("userid")
    def expiry = column[DateTime]("expiry")
    def * = (id, userId, expiry) <> (AuthToken.tupled, AuthToken.unapply _)
  }

  // table query definitions
  val slickUsers = TableQuery[Users]
  val slickLoginInfos = TableQuery[LoginInfos]
  val slickUserLoginInfos = TableQuery[UserLoginInfos]
  val slickPasswordInfos = TableQuery[PasswordInfos]
  val slickOAuth1Infos = TableQuery[OAuth1Infos]
  val slickOAuth2Infos = TableQuery[OAuth2Infos]
  val slickOpenIDInfos = TableQuery[OpenIDInfos]
  val slickOpenIDAttributes = TableQuery[OpenIDAttributes]
  val slickEvents = TableQuery[Events]
  val slickGifts = TableQuery[Gifts]
  val slickParticipants = TableQuery[Participants]
  val slickHistory = TableQuery[HistoryT]
  val slickComment = TableQuery[Comments]
  val slickNotification = TableQuery[Notifications]
  val slickAuthToken = TableQuery[AuthTokens]

  // queries used in multiple places
  def loginInfoQuery(loginInfo: LoginInfo) = 
    slickLoginInfos.filter(dbLoginInfo => dbLoginInfo.providerID === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey)
}
