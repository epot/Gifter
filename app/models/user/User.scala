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
}
