package models.user

import play.api.data._
import play.api.data.Forms._

object UserForms {
  val signInForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText,
      "rememberMe" -> boolean
    )(Data.apply)(Data.unapply)
  )

  /**
    * The form data.
    *
    * @param email The email of the user.
    * @param password The password of the user.
    * @param rememberMe Indicates if the user should stay logged in on the next visit.
    */
  case class Data(
                   email: String,
                   password: String,
                   rememberMe: Boolean)

  val registrationForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "firstname" -> text,
      "lastname" -> text,
      "avatarurl" -> text
    )(RegistrationData.apply)(RegistrationData.unapply)
  )
}
