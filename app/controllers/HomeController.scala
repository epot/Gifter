package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import utils.auth.DefaultEnv

import scala.concurrent.Future

class HomeController @Inject() (val messagesApi: MessagesApi, silhouette: Silhouette[DefaultEnv]) extends Controller with I18nSupport {

  def index = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.userHome(request.identity)))
  }
}
