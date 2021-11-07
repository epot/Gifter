package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.{Method, Request, SendGrid}
import com.sendgrid.helpers.mail.objects.{Content, Email}
import forms.ForgotPasswordForm
import models.services.{AuthTokenService, UserService}
import play.api.{Configuration, Logger}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.JSRouter
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

/**
 * The `Forgot Password` controller.
 *
 * @param components       ControllerComponents
 * @param silhouette       The Silhouette stack.
 * @param userService      The user service implementation.
 * @param authTokenService The auth token service implementation.
 * @param mailerClient     The mailer client.
 * @param webJarAssets     The WebJar assets locator.
 * @param jsRouter         The JS router helper.
 */
class ForgotPasswordController @Inject() (
  components: ControllerComponents,
  configuration: Configuration,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authTokenService: AuthTokenService,
  jsRouter: JSRouter
 )(
  implicit val ex: ExecutionContext)
  extends AbstractController(components) with I18nSupport {
  val logger: Logger = Logger(this.getClass())

  /**
   * Sends an email with password reset instructions.
   *
   * It sends an email to the given address if it exists in the database. Otherwise we do not show the user
   * a notice for not existing email addresses to prevent the leak of existing email addresses.
   *
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async { implicit request =>
    ForgotPasswordForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(Json.obj("errors" -> form.errors.map{_.messages.mkString(", ")}))),
      email => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) if user.email.isDefined =>
            authTokenService.create(user.id).map { authToken =>

              var secure = configuration.getOptional[Boolean]("secure").getOrElse(
                throw new RuntimeException("Cannot get `secure` from config")
              )

              val url = jsRouter.absoluteURL("/reset-password/" + authToken.id, secure)

              val sender = configuration.getOptional[String]("sender").getOrElse(
                throw new RuntimeException("Cannot get `sender` from config")
              )

              val sendgridApiKey = configuration.getOptional[String]("sendgridApiKey").getOrElse(
                throw new RuntimeException("Cannot get `sendgridApiKey` from config")
              )

              val from = new Email(sender)
              val subject = Messages("email.reset.password.subject")
              val to = new Email(email)
              val content = new Content("text/plain", views.txt.emails.resetPassword(user, url).body)
              val mail = new Mail(from, subject, to, content)

              val sg = new SendGrid(sendgridApiKey)
              val emailRequest = new Request()

              emailRequest.setMethod(Method.POST)
              emailRequest.setEndpoint("mail/send")
              emailRequest.setBody(mail.build)
              val response = sg.api(emailRequest)
              logger.info(s"Email sent with status code ${response.getStatusCode}, body ${response.getBody} and headers ${response.getHeaders}")
              Ok
            }
          case _ => Future.successful(Ok)
        }
      }
    )
  }
}
