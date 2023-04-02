package controllers.api

import models.{ Preferences, SESSION_KEY, UNAUTH_MSG, UserDao }
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }

class UserController @Inject()(
  cc: ControllerComponents,
  database: UserDao,
)
  (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def getTargetCalories: Action[AnyContent] = Action.async { request =>
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        database.fetchTargetCalories(userId).map(target => Ok(target.toString))
      }
      .getOrElse {
        Future.successful(Unauthorized(UNAUTH_MSG))
      }
  }

  def getPreferences: Action[AnyContent] = Action.async { request =>
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        database.fetchPreferences(userId).map(target => Ok(Json.toJson(target)))
      }
      .getOrElse {
        Future.successful(Unauthorized(UNAUTH_MSG))
      }
  }

  def updatePreferences(): Action[JsValue] = Action.async(parse.json) { request =>
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        Json.fromJson[Preferences](request.body)
          .asOpt
          .map { preferences =>
            database.updatePreferences(userId, preferences).map { _ =>
              Ok("User preferences successfully updated.")
            }
          }
          .getOrElse {
            Future.successful(BadRequest("Error in processing Json body."))
          }
      }
      .getOrElse {
        Future.successful(Unauthorized(UNAUTH_MSG))
      }
  }

}