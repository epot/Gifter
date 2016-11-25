package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, SignUpEvent, Silhouette}
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfile, CredentialsProvider}
import models.services.UserService
import models.user.{RegistrationData, UserForms}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{AnyContent, Controller, Request}
import models.user.User
import utils.auth.DefaultEnv

import scala.concurrent.Future

class RegistrationController @Inject() (
    val messagesApi: MessagesApi,
    silhouette: Silhouette[DefaultEnv],
    passwordHasherRegistry: PasswordHasherRegistry,
    avatarService: AvatarService,
    authInfoRepository: AuthInfoRepository,
    userService: UserService
) extends Controller with I18nSupport {

  def registrationForm = silhouette.UserAwareAction.async { implicit request =>
    Future.successful(Ok(views.html.signIn(UserForms.registrationForm)))
  }

  def register = silhouette.UnsecuredAction.async { implicit request =>
    UserForms.registrationForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signIn(form))),
      data => {
        userService.retrieve(LoginInfo(CredentialsProvider.ID, data.email)).flatMap {
          case Some(user) => Future.successful {
            Ok(views.html.signIn(UserForms.registrationForm.fill(data))).flashing("error" -> "That email address is already taken.")
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
              email=Some(data.email)
            )  
    val profile = CommonSocialProfile(
      loginInfo = loginInfo,
      email = Some(data.email)
    )
    
    val r = Redirect(controllers.routes.HomeController.index())
    for {
      avatar <- avatarService.retrieveURL(data.email)
      u <- userService.save(user)
      authInfo <- authInfoRepository.add(loginInfo, authInfo)
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
