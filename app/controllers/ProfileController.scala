package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models.services.UserService
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.auth.DefaultEnv

import scala.concurrent.ExecutionContext

class ProfileController @Inject()(
   components: ControllerComponents,
   userService: UserService,
   silhouette: Silhouette[DefaultEnv],
   socialProviderRegistry: SocialProviderRegistry)
  (implicit ec:ExecutionContext) extends AbstractController(components) with I18nSupport {

  def profile = silhouette.SecuredAction { implicit request =>
    Ok(views.html.profile(request.identity, Nil, socialProviderRegistry))
  }
}
