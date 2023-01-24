package controllers

import controllers.customActions.AuthenticatedUserAction
import models.{ LoginDao, LoginData, SESSION_USERNAME_KEY }
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * This controller handles Actions related to logging in.
 */
@Singleton
class LoginController @Inject() (
    cc: ControllerComponents,
    loginDao: LoginDao,
    authenticatedUserAction: AuthenticatedUserAction
  )
  (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  val loginForm: Form[LoginData] = Form(
    mapping (
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginData.apply)(LoginData.unapply)
  )

  /**
   * Takes the username and password from the front-end and checks whether
   * it is the same as in the database.
   * @return Http response on the status of the login process.
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
      Future.successful(BadRequest("Expecting Json data."))
    }
  }

  def test: Action[AnyContent] = Action {
    Ok(Json.obj("test" -> "ing"))
  }

}
