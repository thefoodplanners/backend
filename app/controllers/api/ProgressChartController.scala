package controllers.api

import models.{ ProgressChartDao, SESSION_KEY, UNAUTH_MSG }
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * This controller handles http requests dealing with the progress chart.
 */
class ProgressChartController @Inject()(
  cc: ControllerComponents,
  database: ProgressChartDao
)
  (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  /**
   * Get the metrics for a given date. Metrics include calories, fats, proteins and carbs.
   *
   * @param dateType Type of date. Day/week/month/year.
   * @param date     Actual date.
   * @return Response containing metrics.
   */
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