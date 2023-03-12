package models

import anorm._
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

class ProgressChartDao @Inject()(db: Database)(databaseExecutionContext: DatabaseExecutionContext) {

  private val metricsParser = (
    SqlParser.date("Date") ~
      SqlParser.int("Calories")
  ) map {
    case date ~ caloriesConsumed =>
      ConsumedCalories(date, caloriesConsumed)
  }

  def fetchConsumedCalories(userId: String, dateType: String, dateNum: Int): Future[Seq[ConsumedCalories]] = {
    Future {
      db.withConnection { implicit conn =>
        SQL"""
             SELECT Date, Calories FROM Metrics
             WHERE UserID = $userId
             HAVING #${dateType.toUpperCase}(Date) = $dateNum
             ORDER BY DAY(Date);
           """.as(metricsParser.*)
      }
    }(databaseExecutionContext)
  }

}
