package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import models.daos.EventDAO
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import utils.auth.DefaultEnv
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class HomeController @Inject() (
  val messagesApi: MessagesApi,
  eventDAO: EventDAO,
  silhouette: Silhouette[DefaultEnv]) extends Controller with I18nSupport {

  def index = silhouette.SecuredAction.async { implicit request =>
    eventDAO.findByUser(request.identity).map { events =>
      Ok(views.html.userHome(request.identity, events))
    }
  }

  def giftsDynamicScripts(toDefined: Boolean, username: String) = Action {
    Ok(views.js.gifts.gifts.render(toDefined, username)).as("text/javascript")
  }
}
