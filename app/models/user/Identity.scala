package models.user

import play.api.Play.current
import play.api.db._
import anorm._
import anorm.SqlParser._

case class Identity(
  userId: Pk[Long] = NotAssigned,
  email: String,
  adapter: Identity.Adapter.Value,
  hash: String = "")

object Identity {
  def connectWithPassword(email: String, password: String): Option[User] =
    DB.withConnection { implicit connection =>
      val user = SQL(
        """
         select u.* from identity iden
         join user_table u on iden.userid = u.id
         where iden.adapter = {adapter} and
         iden.email = {email} and iden.hash = {password}
        """
      ).on(
          'email -> email,
          'adapter -> Adapter.UserWithPassword.id,
          'password -> password) 
        .as(User.simple.singleOpt)
        user
    }  

  def updatePassword(userid: Long, password: String) =
    DB.withConnection { implicit connection =>
      val user = SQL(
        """
         update identity set 
         hash = {password}
         from user_table
         where user_table.id = identity.userid 
           and identity.adapter = {adapter}
           and user_table.id = {userid}
        """
      ).on(
          'password -> password,
          'userid -> userid,
          'adapter -> Adapter.UserWithPassword.id) 
        .executeUpdate()
    }  

  /** Below are data added during bootstrap as mandatory DB startup data.
   */
  object Adapter extends Enumeration {
    val UserWithPassword = Value(1, "UserWithPassword")
    val Google = Value(2, "Google")
    val Yahoo = Value(3, "Yahoo")
  }

}