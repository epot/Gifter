package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import net.ceedubs.ficus.Ficus._
import play.api.{Configuration, Logger}
import com.mohiva.play.silhouette.api.util.{Clock, Credentials}
import com.mohiva.play.silhouette.api.{LoginEvent, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.SignInForm
import models.services.UserService
import play.api.cache.AsyncCacheApi
import play.api.mvc._
import utils.auth.DefaultEnv
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utils.Metrics

import scala.language.postfixOps
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

class AuthenticationController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  credentialsProvider: CredentialsProvider,
  configuration: Configuration,
  authInfoRepository: AuthInfoRepository,
  socialProviderRegistry: SocialProviderRegistry,
  userService: UserService,
  cache: AsyncCacheApi,
  clock: Clock)(
 implicit ex: ExecutionContext
) extends AbstractController(components) with I18nSupport {

  val c = Metrics.client

  /**
    * Converts the JSON into a `SignInForm.Data` object.
    */
  implicit val dataReads = (
    (__ \ 'email).read[String] and
      (__ \ 'password).read[String] and
      (__ \ 'rememberMe).read[Boolean]
    )(SignInForm.Data.apply _)

  def authenticateCredentials = Action.async(parse.json) { implicit request =>
    request.body.validate[SignInForm.Data].map { data =>
      credentialsProvider.authenticate(Credentials(data.email, data.password)).flatMap { loginInfo =>
        userService.retrieve(loginInfo).flatMap {
          case Some(user) => silhouette.env.authenticatorService.create(loginInfo).map {
            case authenticator if data.rememberMe =>
              val c = configuration.underlying
              authenticator.copy(
                expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout")
              )
            case authenticator => authenticator
          }.flatMap { authenticator =>
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            silhouette.env.authenticatorService.init(authenticator).map { token =>
              c.increment(name = "giftyou.events.login", tags=Seq("provider:credentials", "user-id:" + user.id))
              Ok(Json.obj("token" -> token))
            }
          }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }.recover {
        case _: ProviderException =>
          BadRequest(Json.obj("message" -> Messages("invalid.credentials")))
      }
    }.recoverTotal {
      case _ =>
        Future.successful(BadRequest(Json.obj("message" -> Messages("invalid.credentials"))))
    }
  }

  def authenticateSocial(provider: String) = Action.async { r =>
    cacheAuthTokenForOauth1(r) { implicit request =>
      (socialProviderRegistry.get[SocialProvider](provider) match {
        case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
          val jsonBody: Option[JsValue] = request.body.asJson
          val authorizationData = jsonBody.map { body =>
            body.\("authorizationData").as[JsObject]
          }.get
          val oauthData = jsonBody.map { body =>
            body.\("oauthData").as[JsObject]
          }
          val userData  = jsonBody.map { body =>
            body.\("userData").as[JsObject]
          }
          val merge = oauthData match {
            case Some(arg2) =>
              val merge = userData match {
                case Some(arg3) =>
                  arg2 ++ arg3
                case _ => arg2
              }
              merge ++ authorizationData
            case _ => authorizationData
          }
          p.authenticate()(request.withBody(AnyContentAsJson(merge))).flatMap {
            case Left(result) => Future.successful(result)
            case Right(authInfo) => for {
              profile <- p.retrieveProfile(authInfo)
              user <- userService.save(profile)
              _ <- authInfoRepository.save(profile.loginInfo, authInfo)
              authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
              token <- silhouette.env.authenticatorService.init(authenticator)
            } yield {
              silhouette.env.eventBus.publish(LoginEvent(user, request))
              c.increment(name = "giftyou.events.login", tags=Seq("provider:" + provider, "user-id:" + user.id))
              Ok(Json.obj("token" -> token))
            }
          }
        case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
      }).recover {
        case e: ProviderException =>
          Logger.error("Unexpected provider error", e)
          Unauthorized(Json.obj("message" -> Messages("could.not.authenticate")))
        case e: Exception =>
          Logger.error("Unexpected error", e)
          Unauthorized(Json.obj("message" -> Messages("could.not.authenticate")))
      }
    }
  }

  /**
    * Satellizer executes multiple requests to the same application endpoints for OAuth1.
    *
    * So this function caches the response from the OAuth provider and returns it on the second
    * request. Not nice, but it works as a temporary workaround until the bug is fixed.
    *
    * @param request The current request.
    * @param f The action to execute.
    * @return A result.
    * @see https://github.com/sahat/satellizer/issues/287
    */
  private def cacheAuthTokenForOauth1(request: Request[AnyContent])(f: Request[AnyContent] => Future[Result]): Future[Result] = {
    request.getQueryString("oauth_token") -> request.getQueryString("oauth_verifier") match {
      case (Some(token), Some(verifier)) => cache.get[Result](token + "-" + verifier).flatMap {
        case Some(result) => Future.successful(result)
        case None => f(request).map { result =>
          cache.set(token + "-" + verifier, result, 1 minute)
          result
        }
      }
      case _ => f(request)
    }
  }

  def signOut = silhouette.SecuredAction.async { implicit request =>
    val result = Redirect("/")
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }
}
