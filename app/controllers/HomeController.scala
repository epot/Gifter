package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import models.daos.EventDAO
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.auth.DefaultEnv

import scala.concurrent.ExecutionContext

class HomeController @Inject() (
  components: ControllerComponents,
  eventDAO: EventDAO,
  silhouette: Silhouette[DefaultEnv])(
  implicit ex: ExecutionContext
) extends AbstractController(components) with I18nSupport {

  def index = silhouette.SecuredAction.async { implicit request =>
    eventDAO.findByUser(request.identity).map { events =>
      Ok(views.html.userHome(request.identity, events))
    }
  }

  def giftsDynamicScripts(toDefined: Boolean, username: String) = Action {
    Ok(views.js.gifts.gifts.render(toDefined, username)).as("text/javascript")
  }
}
