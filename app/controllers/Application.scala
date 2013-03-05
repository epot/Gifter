package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.openid._

import views._

import play.api._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits._
import libs.openid.{UserInfo, OpenID}
import play.api.mvc._
import play.api.mvc.Results._

object Application extends Controller {

  // -- Actions
  /**
   * Describes the hello form.
   */
  val loginForm = Form(
    single(
      "openid" -> text
    )
  )
  
  val REQUIRED_ATTRIBUTES=Seq(
    "email" -> "http://schema.openid.net/contact/email"
  )
  
  /**
   * Home page
   */
  def index = Action {
    Ok(html.index(loginForm))
  }
  
  def loginPost = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      error => {
        Logger.info("bad request " + error.toString)
        BadRequest(error.toString)
      },
      {
        case (openid) => 
          val openIdCallbackUrl: String = routes.Application.openIDCallback().absoluteURL()
          def onRedirected(promise: NotWaiting[String]): Result = {
            promise match {
              case Redeemed(url) => Redirect(url)
              case Thrown(throwable) => Unauthorized("Unable to verify your openid provider.<br>"+throwable.getMessage)
            }
          }
          AsyncResult(
            OpenID.redirectURL("https://www.google.com/accounts/o8/id", openIdCallbackUrl, REQUIRED_ATTRIBUTES).extend1(onRedirected)
          )
      }
    )
  }
  
  def openIDCallback = Action { implicit request =>
    def onVerified(promise:NotWaiting[UserInfo]):Result ={
      promise match {
        case Redeemed(info) => 
          println("Watchaaaaaaaaa "+ info.attributes.get("email").get)
          Ok(html.index(loginForm))
        case Thrown(throwable) => Unauthorized("Authorization refused by your openid provider<br>"+throwable.getMessage)
      }
    }
    AsyncResult(
      OpenID.verifiedId.extend1(onVerified)
    )
  }

}
