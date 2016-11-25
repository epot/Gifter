package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import jdub.async.Database
import models.services.UserService
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import utils.auth.DefaultEnv

import scala.concurrent.ExecutionContext

class ProfileController @Inject()
  (val messagesApi: MessagesApi,
   userService: UserService,
   silhouette: Silhouette[DefaultEnv],
   socialProviderRegistry: SocialProviderRegistry)
  (implicit ec:ExecutionContext) extends Controller with I18nSupport {

  def profile = silhouette.SecuredAction { implicit request =>
    Ok(views.html.profile(request.identity, Nil, socialProviderRegistry))
  }
}
