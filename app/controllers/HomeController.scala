package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import play.api.{Environment, Mode, Configuration}
import play.api.i18n.{I18nSupport, MessagesApi}
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
  conf: Configuration,
  environment: Environment,
  messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv])(
  implicit val ex: ExecutionContext
) extends AbstractController(components) with I18nSupport {

  /**
    * Renders the UI component with the index route.
    *
    * @return The ui component.
    */
  def index: Action[AnyContent] = serveUI("index.html")

  /**
    * Renders the UI component with the given route.
    *
    * @param route The UI route.
    * @return The ui component.
    */
  def route(route: String): Action[AnyContent] = serveUI(route)


  /**
    * Serves the UI.
    *
    * In development mode it serves the ui app through the started node.js server. In production Play serves
    * the files through the asset pipeline.
    *
    * @param route The UI route.
    * @return The ui component.
    */
  private def serveUI(route: String): Action[AnyContent] = Action.async { request =>
    environment.mode match {
      case Mode.Prod  => assets.versioned("/public", "ui/" + route)(request)
      case _ =>
        Future.successful(Redirect(conf.getOptional[String]("ui.dev.url").getOrElse(
          throw new RuntimeException("Cannot get `ui.dev.url` from config")
        )))
    }
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
