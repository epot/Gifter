package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import play.api.{Environment, Mode}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}

import models.daos.EventDAO
import models.JsonFormat._
import utils.auth.DefaultEnv


class HomeController @Inject() (
  ws: WSClient,
  assets: Assets,
  components: ControllerComponents,
  eventDAO: EventDAO,
  environment: Environment,
  silhouette: Silhouette[DefaultEnv])(
  implicit ex: ExecutionContext
) extends AbstractController(components) with I18nSupport {

  /*
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.index()))
  }

  def bundle(file: String): Action[AnyContent] = if (environment.mode == Mode.Dev) Action.async {
    ws.url(s"http://localhost:8080/bundles/$file").get().map { response =>
      val contentType = response.headers.get("Content-Type").flatMap(_.headOption).getOrElse("application/octet-stream")
      val headers = response.headers
        .toSeq.filter(p => List("Content-Type", "Content-Length").indexOf(p._1) < 0).map(p => (p._1, p._2.mkString))
      Ok(response.body).withHeaders(headers: _*).as(contentType)
    }
  }
  else {
    assets.at("public/bundles", file)
  }

  /**
    * Returns the user.
    *
    * @return The result to display.
    */

  def user = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(Json.toJson(request.identity)))
  }

}
