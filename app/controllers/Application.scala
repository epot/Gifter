package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.openid._

import models.user._
import views._

import play.api._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits._
import libs.openid.{UserInfo, OpenID}
import play.api.mvc._
import play.api.mvc.Results._

object Application extends Controller {

  // -- Actions
  
  def logout = Action{
    Redirect(routes.UserApplication.index).withNewSession
  }
  
  val REQUIRED_ATTRIBUTES=Seq(
    "email" -> "http://schema.openid.net/contact/email",
    "firstname" -> "http://axschema.org/namePerson/first",
    "lastname" -> "http://axschema.org/namePerson/last"
  )  
  
  /**
   * represents a sort of backing bean to back the login form.
   * It describes the content of the form and the validation.
   * This object is used in the template and the response
   */
  val loginForm = Form {
    tuple(
      "email" -> email,
      "password" -> text
    ) verifying ("Invalid email or password", result => result match {
          case (email, password) => Identity.connectWithPassword(email, password).isDefined
        }
      )
  }
  
  /**
   * Home page
   */
  def index = Action {
    Ok(html.index(loginForm))
  }
  
  def googleLogin = Action { implicit request =>
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
  
  def openIDCallback = Action { implicit request =>
    def onVerified(promise:NotWaiting[UserInfo]):Result ={
      promise match {
        case Redeemed(info) => 
          val email = info.attributes.get("email").get
          val user = User.findByEmail(email) match {
            case Some(user) => user
            case None => {
              val user = User.create(
                  User(name=info.attributes.get("firstname").get + " " + info.attributes.get("lastname").get), 
                  Identity(email=email, adapter=Identity.Adapter.Google) )
              user
            }
          }
          
          Redirect(routes.UserApplication.index).withSession("userId" -> user.id.toString)
          
        case Thrown(throwable) => Unauthorized("Authorization refused by your openid provider<br>"+throwable.getMessage)
      }
    }
    AsyncResult(
      OpenID.verifiedId.extend1(onVerified)
    )
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.index(formWithErrors)),
      email_pass => {
        val user = User.findByEmail(email_pass._1).get
        Logger.info("Login successful")
        Redirect(routes.UserApplication.index).withSession("userId" -> user.id.toString)
      }
    )
  }

}
