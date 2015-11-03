package controllers

import java.util.UUID

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.{ LoginEvent, LogoutEvent }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.{ CommonSocialProfile, CommonSocialProfileBuilder, SocialProvider }
import models.user.{ User, UserForms }
import play.api.i18n.MessagesApi
import services.user.AuthenticationEnvironment
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action

import scala.concurrent.Future

@javax.inject.Singleton
class AuthenticationController @javax.inject.Inject() (
    override val messagesApi: MessagesApi,
    override val env: AuthenticationEnvironment
) extends BaseController {
  
  def signInForm = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Ok(views.html.userHome(user)))
      case None => Future.successful(Ok(views.html.index(UserForms.signInForm)))
    }
  }

  def authenticateCredentials = UserAwareAction.async { implicit request =>
    UserForms.signInForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.index(form))),
      credentials => env.credentials.authenticate(credentials).flatMap { loginInfo =>
        val result = Redirect(controllers.routes.HomeController.index())
        env.identityService.retrieve(loginInfo).flatMap {
          case Some(user) => env.authenticatorService.create(loginInfo).flatMap { authenticator =>
            println("authentified " + user)
            env.eventBus.publish(LoginEvent(user, request, request2Messages))
            env.authenticatorService.init(authenticator).flatMap(v => env.authenticatorService.embed(v, result))
          }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user."))
        }
      }.recover {
        case e: ProviderException =>
          Redirect(controllers.routes.AuthenticationController.signInForm()).flashing(("error", "Invalid credentials."))
      }
    )
  }

  def authenticateSocial(provider: String) = UserAwareAction.async { implicit request =>
    (env.providersMap.get(provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => {
            Future.successful(result)
          }
          case Right(authInfo) => {
            for {
              profile <- p.retrieveProfile(authInfo)
              user <- env.userService.create(mergeUser(request.identity, profile), profile)
              authInfo <- env.authInfoService.save(profile.loginInfo, authInfo)
              authenticator <- env.authenticatorService.create(profile.loginInfo)
              value <- env.authenticatorService.init(authenticator)
              result <- env.authenticatorService.embed(value, Redirect(controllers.routes.HomeController.index()))
            } yield {
              env.eventBus.publish(LoginEvent(user, request, request2Messages))
              result
            }
          }
        }
      case _ => Future.failed(new ProviderException("Invalid provider [" + provider + "]."))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect(routes.AuthenticationController.signInForm()).flashing(("error", "Service error with provider [" + provider + "]."))
    }
  }

  def signOut = SecuredAction.async { implicit request =>
    val result = Redirect(controllers.routes.HomeController.index())
    env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))
    env.authenticatorService.discard(request.authenticator, result).map(x => result)
  }

  private[this] def mergeUser(potential_user: Option[User], profile: CommonSocialProfile) = {
    potential_user match {
      case Some(user) => {
        user.copy(
          username = if (profile.firstName.isDefined && user.username.isEmpty) { profile.firstName } else { user.username }
        )
      }
      case None => {
        User(
          id = UUID.randomUUID(),
          username = profile.firstName,
          profiles = Nil
        )
      }
    }
  }
}
