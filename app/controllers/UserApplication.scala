package controllers

import models.user._
import models._
import play.api.mvc._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._

trait Secured {

  /**
   * Retrieve the connected user email.
   */
  private def userId(request: RequestHeader) = request.session.get("userId")

  /**
   * Redirect to login if the user in not authorized.
   */
  def onUnauthorized() = Results.Redirect(routes.Application.index)
  // the one below is just to comply to the signature asked by Security.Authenticated
  def onUnauthorized(request: RequestHeader): Result = onUnauthorized()

  /**
   * Action for authenticated users. The bodyParser argument is to be able to specify, for example, a json parser.
   */
  def IsAuthenticated[A](bodyParser: BodyParser[A])(f: => User => Request[A] => Result) = Security.Authenticated(userId, onUnauthorized) { userId =>
    User.findById(userId.toLong) match {
      case Some(user) => Action(bodyParser)(request => f(user)(request))
      case _ => Action(bodyParser)(request => onUnauthorized())
    }
  }
  def IsAuthenticated(f: => User => Request[AnyContent] => Result): play.api.mvc.EssentialAction =
    IsAuthenticated(BodyParsers.parse.anyContent)(f)
}

object UserApplication extends Controller with Secured {

  def index = IsAuthenticated { user => implicit request =>
    Ok(views.html.userHome(user))
  }

}
