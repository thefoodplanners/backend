package controllers

import controllers.customActions.AuthenticatedUserAction
import models.{ LoginDao, LoginData, RegisterData, SESSION_KEY }
import play.api.libs.json.{ JsValue, Json }
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

  /**
   * Takes the username and password from the front-end and checks whether
   * it is the same as in the database.
   * @return Http response on the status of the login process.
   */
  def processLogin: Action[JsValue] = Action.async(parse.json) { request =>
    Json.fromJson[LoginData](request.body)
      .asOpt
      .map(loginDao.checkLoginDetails)
      .map(_.map { case (isAuthorised, userId) =>
        if (isAuthorised) {
          Ok("Login successful.")
            .withSession(SESSION_KEY -> userId.toString)
        }
        else Unauthorized("Username and/or password is incorrect.")
      })
      .getOrElse {
        Future.successful(BadRequest("Error in login form."))
      }
  }
  
  def logout: Action[AnyContent] = Action {
    Ok("Successfully logged out.").withNewSession
  }

  def register: Action[JsValue] = Action.async(parse.json) { request =>
    Json.fromJson[RegisterData](request.body)
      .asOpt
    ???
  }

  def test: Action[AnyContent] = Action {
    Ok(Json.obj("test" -> "ing"))
  }

}
