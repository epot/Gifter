package models.user

case class RegistrationData(
  username: String,
  email: String,
  password: String,
  firstName: String,
  lastName: String,
  avatarURL: String
)
