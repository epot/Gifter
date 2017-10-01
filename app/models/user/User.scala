package models.user

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import org.joda.time.LocalDateTime

case class User(
    id: UUID,
    profiles: Seq[LoginInfo],
    firstName: Option[String]=None,
    lastName: Option[String]=None,
    fullName: Option[String],
    email: Option[String],
    avatarURL: Option[String]=None
) extends Identity {
  def isGuest = profiles.isEmpty
  override def equals(that: Any) = {
    that match {
      case that: User => that.canEqual(this) && this.id == that.id
      case _ => false
    }
  }

  def userName = firstName match {
    case Some(name) => name
    case _ => fullName.getOrElse("Rene Coty")
  }
}
