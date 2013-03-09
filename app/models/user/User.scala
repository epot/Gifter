package models.user

import java.text._
import java.security.MessageDigest
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

/**User specs:
 * - a user is currently very simple but it is going to be the most complex object of them all ...
 */
case class User(
  id: Pk[Long] = NotAssigned,
  name: String,
  isNotMember: Boolean = false) {
}

object User {

  val simple =
    get[Pk[Long]]("user.id") ~
    get[String]("user.name") ~
    get[Boolean]("user.isNotMember") map {
      case id~name~isNotMember => 
        User(id, name, isNotMember)
    }
  
  def create(user: User, identity: Identity) = {
    DB.withConnection { implicit connection =>
      // Get the user id
      val id: Long = user.id.getOrElse {
        SQL("select next value for user_seq").as(scalar[Long].single)
      }
      
      SQL(
        """
          insert into user values (
            {id}, {name}, {isNotMember}
          )
        """
      ).on(
        'id -> id,
        'name -> user.name,
        'isNotMember -> user.isNotMember
      ).executeUpdate()
      
      SQL(
        """
            insert into identity values (
              {userId}, {email}, {adapter}, {hash}
            )
        """
        ).on(
          'userId -> id,
          'email -> identity.email,
          'adapter -> identity.adapter.id,
          'hash -> identity.hash
      ).executeUpdate()

      
      user.copy(id = Id(id))
    }
  }
  
  def update(userId: Long, user: User) = 
  DB.withConnection{ implicit connectin =>
    SQL("""
        update user set 
        name = {name}, isNotMember = {isNotMember}
        where id = {id}
        """).on(
        'id -> userId,
        'fullname -> user.name,
        'isNotMember -> user.isNotMember
      ).executeUpdate()
  }

  def findByEmail(email: String): Option[User] =
    DB.withConnection { implicit connection =>
      val user = SQL(
        """
         select u.* from identity iden
         join user u on iden.userId = u.id
         where iden.email = {email}
        """
      ).on(
          'email -> email) 
        .as(User.simple.singleOpt)
        user
  }
  
  def findById(id: Long): Option[User] =
  DB.withConnection{ implicit connection =>
    SQL("select * from user where id = {id}")
      .on('id -> id)
      .as(User.simple.singleOpt)
  }
  
  def findByIds(ids: List[Long]): List[User] =
  DB.withConnection{ implicit connection =>
    SQL("select * from user where id in (" + ids.mkString(",") + ")")
      .as(User.simple *)
  }
  
  def findAll: List[User] =
  DB.withConnection{ implicit connection =>
    SQL("select * from user")
      .as(User.simple *)    
  }
  
  def count: Long = 
  DB.withConnection{ implicit connection =>
    SQL("select count(*) as c from user").as(scalar[Long].single)
  }
}