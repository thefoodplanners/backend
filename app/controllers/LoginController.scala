package controllers

import models.{LoginDao, LoginData, SESSION_USERNAME_KEY}
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class LoginController @Inject() (loginDao: LoginDao, cc: ControllerComponents)
  (implicit ec: ExecutionContext)
  extends AbstractController(cc)
  with play.api.i18n.I18nSupport {

  val loginForm: Form[LoginData] = Form(
    mapping (
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginData.apply)(LoginData.unapply)
  )

  def index(): Action[AnyContent] = Action { implicit request =>
    request.session
    Ok(views.html.index(loginForm, false))
  }

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def login(): Action[AnyContent] = Action.async { implicit request =>
    loginForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(views.html.index(formWithErrors, false))),
      loginData => {
        val user = LoginData(loginData.username, loginData.password)
        loginDao.checkLoginDetails(user).map { isAuthorised =>
          if (isAuthorised) Redirect(routes.LoginController.home(user.username, user.password))
          else Unauthorized(views.html.index(loginForm.fill(loginData), true))
        }
      }
    )
  }

  def home(username: String, password: String): Action[AnyContent] = Action {
    Ok(views.html.mainPage(username, password))
  }

}
