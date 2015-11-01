package models.user

import com.mohiva.play.silhouette.api.util.Credentials
import play.api.data._
import play.api.data.Forms._

object UserForms {
  val signInForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(Credentials.apply)(Credentials.unapply)
  )

  val registrationForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "email" -> nonEmptyText,"password" -> tuple(
          "main" -> nonEmptyText,
          "confirm" -> text
        ).verifying(
          // Add an additional constraint: both passwords must match
          "Passwords do not match", passwords => passwords._1 == passwords._2
      )
    )(RegistrationData.apply)(RegistrationData.unapply)
  )
}
