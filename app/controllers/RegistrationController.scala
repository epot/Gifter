package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, SignUpEvent, Silhouette}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.services.UserService
import models.user.{RegistrationData, UserForms}
import play.api.i18n.I18nSupport
import play.api.mvc._
import models.user.User
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

class RegistrationController @Inject() (
    components: ControllerComponents,
    silhouette: Silhouette[DefaultEnv],
    passwordHasherRegistry: PasswordHasherRegistry,
    avatarService: AvatarService,
    authInfoRepository: AuthInfoRepository,
    userService: UserService
)(implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  def registrationForm = silhouette.UserAwareAction.async { implicit request =>
    Future.successful(Ok(views.html.signIn(UserForms.registrationForm)))
  }

  def register = silhouette.UnsecuredAction.async { implicit request =>
    UserForms.registrationForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signIn(form))),
      data => {
        userService.retrieve(LoginInfo(CredentialsProvider.ID, data.email)).flatMap {
          case Some(_) => Future.successful {
            Redirect(routes.RegistrationController.registrationForm).flashing("error" -> "That email address is already taken.")
          }
          case None => saveProfile(data)
        }
      }
    )
  }

  private[this] def saveProfile(data: RegistrationData)(implicit request: Request[AnyContent]) = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
    val authInfo = passwordHasherRegistry.current.hash(data.password._1)
    val user = User(
              id = UUID.randomUUID(),
              profiles = Seq(loginInfo),
              fullName = Some(data.username),
              email=Some(data.email),
              avatarURL = Some(data.avatarURL),
              firstName = Some(data.firstName),
              lastName = Some(data.lastName)
            )  

    val r = Redirect(controllers.routes.HomeController.index())
    for {
      _ <- avatarService.retrieveURL(data.email)
      u <- userService.save(user)
      _ <- authInfoRepository.add(loginInfo, authInfo)
      authenticator <- silhouette.env.authenticatorService.create(loginInfo)
      value <- silhouette.env.authenticatorService.init(authenticator)
      result <- silhouette.env.authenticatorService.embed(value, r)
    } yield {
      silhouette.env.eventBus.publish(SignUpEvent(u, request))
      silhouette.env.eventBus.publish(LoginEvent(u, request))
      result
    }
  }
}
