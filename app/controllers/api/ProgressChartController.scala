package controllers.api

import models.{ ProgressChartDao, SESSION_KEY, UNAUTH_MSG }
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }

class ProgressChartController @Inject()(
  cc: ControllerComponents,
  database: ProgressChartDao
)
  (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def getMetrics(dateType: String, date: String): Action[AnyContent] = Action.async { request =>
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        database.fetchMetrics(userId, dateType, date).map { allMetrics =>
          Ok(Json.toJson(allMetrics))
        }
      }
      .getOrElse {
        Future.successful(Unauthorized(UNAUTH_MSG))
      }
  }

}