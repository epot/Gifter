package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import com.mohiva.play.silhouette.api.util.{Clock, Credentials}
import com.mohiva.play.silhouette.api.{Logger, LoginEvent, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import models.user.{User, UserForms}
import models.services.user.{UserSearchService, UserService}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, Controller}
import utils.auth.DefaultEnv

import scala.language.postfixOps
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

@javax.inject.Singleton
class AuthenticationController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  credentialsProvider: CredentialsProvider,
  configuration: Configuration,
  authInfoRepository: AuthInfoRepository,
  socialProviderRegistry: SocialProviderRegistry,
  userService: UserService,
  clock: Clock) extends Controller with I18nSupport with Logger {

  def signInForm = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.index(UserForms.signInForm, socialProviderRegistry)))
  }

  def authenticateCredentials = silhouette.UnsecuredAction.async { implicit request =>
    logger.info("slip")
    UserForms.signInForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.index(form, socialProviderRegistry))),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          val result = Redirect(routes.HomeController.index())
          UserSearchService.retrieve(loginInfo).flatMap {
            case Some(user) =>
              val c = configuration.underlying
              silhouette.env.authenticatorService.create(loginInfo).map {
                case authenticator if data.rememberMe =>
                  authenticator.copy(
                    expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                    idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout"),
                    cookieMaxAge = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.cookieMaxAge")
                  )
                case authenticator => authenticator
              }.flatMap { authenticator =>
                silhouette.env.eventBus.publish(LoginEvent(user, request))
                silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
                  silhouette.env.authenticatorService.embed(v, result)
                }
              }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case e: ProviderException =>
            Redirect(controllers.routes.AuthenticationController.signInForm()).flashing(("error", "Invalid credentials."))
        }
      }
    )
  }

  def authenticateSocial(provider: String) = silhouette.UserAwareAction.async { implicit request =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.save(profile)
            authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
            authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
            value <- silhouette.env.authenticatorService.init(authenticator)
            result <- silhouette.env.authenticatorService.embed(value, Redirect(routes.HomeController.index()))
          } yield {
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            result
          }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect(routes.AuthenticationController.signInForm()).flashing(("error", "Service error with provider [" + provider + "]."))
    }
  }

  def signOut = silhouette.SecuredAction.async { implicit request =>
    val result = Redirect(routes.HomeController.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }
}
