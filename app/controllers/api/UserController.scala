package controllers.api

import models.{ SESSION_KEY, UNAUTH_MSG, UserDao }
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

}