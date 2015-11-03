package controllers

import java.util.UUID

import com.mohiva.play.silhouette.api.{ LoginEvent, LoginInfo, SignUpEvent }
import com.mohiva.play.silhouette.impl.providers.{ CommonSocialProfile, CredentialsProvider }
import models.user.{ RegistrationData, UserForms }
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.AnyContent
import services.user.AuthenticationEnvironment
import play.api.mvc.Action
import play.api.mvc.Request
import models.user.User

import scala.concurrent.Future

@javax.inject.Singleton
class RegistrationController @javax.inject.Inject() (
    override val messagesApi: MessagesApi,
    override val env: AuthenticationEnvironment
) extends BaseController {

  def registrationForm = UserAwareAction.async { implicit request =>
    Future.successful(Ok(views.html.signIn(UserForms.registrationForm)))
  }

  def register = Action.async { implicit request =>
    UserForms.registrationForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signIn(form))),
      data => {
        env.identityService.retrieve(LoginInfo(CredentialsProvider.ID, data.email)).flatMap {
          case Some(user) => Future.successful {
            Ok(views.html.signIn(UserForms.registrationForm.fill(data))).flashing("error" -> "That email address is already taken.")
          }
          case None => env.identityService.retrieve(data.username) flatMap {
            case Some(user) => Future.successful {
              Ok(views.html.signIn(UserForms.registrationForm.fill(data))).flashing("error" -> "That username is already taken.")
            }
            case None => {
              saveProfile(data)
            }
          }
        }
      }
    )
  }

  private[this] def saveProfile(data: RegistrationData)(implicit request: Request[AnyContent]) = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
    val authInfo = env.hasher.hash(data.password._1)
    val user = User(
              id = UUID.randomUUID(),
              username = Some(data.username),
              profiles = Seq(loginInfo)
            )  
    val profile = CommonSocialProfile(
      loginInfo = loginInfo,
      email = Some(data.email)
    )
    
    val r = Redirect(controllers.routes.HomeController.index())
    for {
      avatar <- env.avatarService.retrieveURL(data.email)
      profile <- env.userService.create(user, profile.copy(avatarURL = avatar.orElse(Some("default"))))
      u <- env.userService.save(user, update = true)
      authInfo <- env.authInfoService.save(loginInfo, authInfo)
      authenticator <- env.authenticatorService.create(loginInfo)
      value <- env.authenticatorService.init(authenticator)
      result <- env.authenticatorService.embed(value, r)
    } yield {
      env.eventBus.publish(SignUpEvent(u, request, request2Messages))
      env.eventBus.publish(LoginEvent(u, request, request2Messages))
      result
    }
  }
}
