package models

import anorm._
import org.joda.time.LocalDate
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

class ProgressChartDao @Inject()(db: Database)(databaseExecutionContext: DatabaseExecutionContext) {

  private val metricsParser = (
    SqlParser.date("Date") ~
      SqlParser.int("SUM(Calories)")
  ) map {
    case date ~ caloriesConsumed =>
      ConsumedCalories(date.toString, caloriesConsumed)
  }

  def fetchConsumedCalories(userId: String, dateType: String, dateNum: Int): Future[Seq[ConsumedCalories]] = {
    Future {
      db.withConnection { implicit conn =>
        SQL"""
             SELECT Date, SUM(Calories) FROM Meal_Slot ms
             JOIN Recipe r ON ms.RecipeID = r.RecipeID
             WHERE ms.UserID = $userId
             GROUP BY Date
             HAVING #${dateType.toUpperCase}(Date, 1) = $dateNum
           """.as(metricsParser.*)
      }
    }(databaseExecutionContext)
  }

}
