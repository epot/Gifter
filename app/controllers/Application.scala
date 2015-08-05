package controllers

import javax.inject.Inject

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.openid._
import play.api.Play.current
import play.api.i18n.{MessagesApi, I18nSupport}

import models.user._
import views._

import play.api._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits._
import libs.openid.{UserInfo, OpenID}
import play.api.mvc._
import play.api.mvc.Results._
import anorm._

import scala.util.{Try, Success, Failure}

class Application @Inject() (val messagesApi: MessagesApi) 
  extends Controller with I18nSupport {

  // -- Actions
  
  def logout = Action{
    Redirect(routes.UserApplication.index).withNewSession
  }
  
  val REQUIRED_ATTRIBUTES=Seq(
    "email" -> "http://schema.openid.net/contact/email"
    ,"firstname" -> "http://axschema.org/namePerson/first"
    //,"lastname" -> "http://axschema.org/namePerson/last"
  )  
  val REQUIRED_ATTRIBUTES_yahoo=Seq(
    "email" -> "http://axschema.org/contact/email"
    ,"nickname" -> "http://axschema.org/namePerson/friendly"
    //,"lastname" -> "http://axschema.org/namePerson/last"
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
  
  def googleLogin = Action.async { implicit request =>
    val openIdCallbackUrl: String = routes.Application.openIDCallback().absoluteURL()
    OpenID.redirectURL("https://www.google.com/accounts/o8/id", openIdCallbackUrl, REQUIRED_ATTRIBUTES).map(url => Redirect(url))
        .recover { case t: Throwable => Redirect(routes.Application.index) }
  }
  
  def yahooLogin = Action.async { implicit request =>
    val openIDCallbackYahoo: String = routes.Application.openIDCallbackYahoo().absoluteURL()
    OpenID.redirectURL("https://me.yahoo.com ", openIDCallbackYahoo, REQUIRED_ATTRIBUTES_yahoo).map(url => Redirect(url))
        .recover { case t: Throwable => Redirect(routes.Application.index) }
  }
  
  def openIDCallback = Action.async { implicit request =>
    OpenID.verifiedId.map(info => {
        val email = info.attributes.get("email").get
        val user = User.findByEmail(email) match {
          case Some(user) => user
          case None => {
            val user = User.create(
                User(name=info.attributes.get("firstname").get), 
                Identity(email=email, adapter=Identity.Adapter.Google) )
            user
          }
        }
        
        Redirect(routes.UserApplication.index).withSession("userId" -> user.id.toString)
      })
    .recover {
      case t: Throwable =>
      Unauthorized("Authorization refused by Google<br>"+t.getMessage)
    }
  }

  def openIDCallbackYahoo = Action.async { implicit request =>
    OpenID.verifiedId.map(info => {
        val email = info.attributes.get("email").get
        val user = User.findByEmail(email) match {
          case Some(user) => user
          case None => {
            val user = User.create(
                User(name=info.attributes.get("nickname").get), 
                Identity(email=email, adapter=Identity.Adapter.Yahoo) )
            user
          }
          }
        
        Redirect(routes.UserApplication.index).withSession("userId" -> user.id.toString)
      })
    .recover {
      case t: Throwable =>
      Unauthorized("Authorization refused by Yahoo<br>"+t.getMessage)
    }
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

  val signInForm = Form[(User, Identity)](
    mapping(
      "name" -> nonEmptyText,
      "email" -> email.verifying("This email address is already used by another account", email => User.findByEmail(email).isEmpty),
      "password" -> tuple(
            "main" -> nonEmptyText,
            "confirm" -> text
          ).verifying(
            // Add an additional constraint: both passwords must match
            "Passwords don't match", passwords => passwords._1 == passwords._2
      )
    )
    {/*apply*/
      (name, email, password) =>
        {
          (User(name=name), Identity(email=email, adapter=Identity.Adapter.UserWithPassword, hash=password._1))
        }
    }
    {/*unapply*/
      userTuple =>
        Some(
            userTuple._1.name,
            userTuple._2.email,
            (userTuple._2.hash, userTuple._2.hash)
        )
    }
  )

  def signIn = Action{ implicit request =>
    Ok{
      views.html.signIn(signInForm)
    }
  }

  def postSignIn() =
    Action { implicit request =>
        signInForm.bindFromRequest.fold(
            // password fails
          formWithErrors => {
            Logger.debug("Could not sign in, the form contains errors: "+formWithErrors.errors)
            BadRequest(views.html.signIn(formWithErrors))
          },
            // password OK
          (userTuple) => {
            val userWithId = User.create(userTuple._1, userTuple._2)
            Logger.debug("Profile successfully created.")
            Redirect(routes.UserApplication.index).withSession("userId" -> userWithId.id.toString)
          }
        )
    }  
  
}
