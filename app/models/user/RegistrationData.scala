package models.user

case class RegistrationData(
  username: String,
  email: String,
  password: (String, String),
  firstName: String,
  lastName: String,
  avatarURL: String
)
