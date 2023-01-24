package controllers

import controllers.customActions.AuthenticatedUserAction
import models.{ LoginDao, LoginData, SESSION_USERNAME_KEY }
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{ JsResult, JsValue, Json }
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class LoginController @Inject() (
    cc: ControllerComponents,
    loginDao: LoginDao,
    authenticatedUserAction: AuthenticatedUserAction
  )
  (implicit ec: ExecutionContext)
  extends AbstractController(cc)
  with play.api.i18n.I18nSupport {

  val loginForm: Form[LoginData] = Form(
    mapping (
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginData.apply)(LoginData.unapply)
  )

  def index: Action[AnyContent] = Action { implicit request =>
    request.session
    Ok(views.html.index())
  }

  def showLoginForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.loginPage(loginForm))
  }

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def processLogin: Action[AnyContent] = Action.async { request =>
    request.body.asJson.map { json =>
      Json.fromJson[LoginData](json)
        .asOpt
        .map(loginDao.checkLoginDetails)
        .map(_.map { isAuthorised =>
          if (isAuthorised) {
            Ok("Login successful.")
              .withSession(SESSION_USERNAME_KEY -> (json \ "username").asOpt[String].get)
          }
          else Unauthorized("Username and/or password is incorrect.")
        })
        .getOrElse {
          Future.successful(BadRequest("Error in login form."))
        }
    }.getOrElse {
      Future.successful(BadRequest("Error in sent JSON."))
    }
  }

  def home: Action[AnyContent] =  Action { implicit request =>
    Ok(views.html.mainPage(routes.LogoutController.logout))
  }

}
