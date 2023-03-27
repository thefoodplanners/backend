package controllers.api

import models.{ ProgressChartDao, SESSION_KEY, UNAUTH_MSG }
import org.joda.time.LocalDate
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

  def getWeeklyCalories(dateType: String, date: String): Action[AnyContent] = Action.async { request =>
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        val localDate = LocalDate.parse(date)

        val dateNum = dateType match {
          case "day" => localDate.getWeekOfWeekyear
          case "week" => localDate.getWeekOfWeekyear
          case "month" => localDate.getMonthOfYear
          case "year" => localDate.getYear
        }

        database.fetchConsumedCalories(userId, dateType, dateNum).map { allConsumedCalories =>
          Ok(Json.toJson(allConsumedCalories))
        }
      }
      .getOrElse {
        Future.successful(Unauthorized(UNAUTH_MSG))
      }
  }

}