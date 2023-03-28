package controllers.api

import models.{ LoginDao, LoginData, RegisterData, SESSION_KEY }
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * This controller handles Actions related to logging in.
 */
@Singleton
class LoginController @Inject()(
  cc: ControllerComponents,
  database: LoginDao
)
  (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  /**
   * Takes the username and password from the front-end and checks whether
   * it is the same as in the database.
   *
   * @return Http response on the status of the login process.
   */
  def processLogin: Action[JsValue] = Action.async(parse.json) { request =>
    Json.fromJson[LoginData](request.body)
      .asOpt
      .map(database.checkLoginDetails)
      .map(_.map { userIdOpt =>
        if (userIdOpt.nonEmpty) {
          Ok("Login successful.")
            .withSession(SESSION_KEY -> userIdOpt.get.toString)
        }
        else Unauthorized("Username and/or password is incorrect.")
      }
      )
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
      .map(database.addNewUser)
      .map(_.map { success =>
        if (success) Ok("User successfully added.")
        else InternalServerError("Error. User not added correctly.")
      })
      .getOrElse {
        Future.successful(BadRequest("Error in processing Json data in request body."))
      }
  }

  def test: Action[AnyContent] = Action { request =>
    request.session
      .get(SESSION_KEY)
      .map(_ => Ok("true"))
      .getOrElse(Ok("false"))
  }

}
