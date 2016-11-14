package models.user

import com.mohiva.play.silhouette.api.{ Authenticator, Authorization }
import play.api.i18n._
import play.api.mvc.Request

import scala.concurrent.Future

sealed trait Role extends Serializable {
  def name: String
}

object Role {
  def apply(role: String): Role = role match {
    case Admin.name => Admin
    case User.name => User
    case _ => Unknown
  }

  def unapply(role: Role): Option[String] = Some(role.name)

  object Admin extends Role {
    val name = "admin"
  }

  object User extends Role {
    val name = "user"
  }

  object Unknown extends Role {
    val name = "-"
  }
}
