package models

import anorm._
import org.joda.time.LocalDate
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

class ProgressChartDao @Inject()(db: Database)(databaseExecutionContext: DatabaseExecutionContext) {

  private val metricsParser = (
    SqlParser.str("Date_Name") ~
      SqlParser.int("Calories_Consumed")
  ) map {
    case date ~ caloriesConsumed =>
      ConsumedCalories(date, caloriesConsumed)
  }

  def fetchConsumedCalories(userId: String, dateType: String, dateNum: Int): Future[Seq[ConsumedCalories]] = {
    Future {
      db.withConnection { implicit conn =>
        val sqlQuery = dateType match {
          case "day" =>
            SQL"""
                  SELECT DAYNAME(Date) AS Date_Name, Calories_Consumed
                  FROM (
                    SELECT Date, SUM(Calories) AS Calories_Consumed
                    FROM Meal_Slot ms
                    JOIN Recipe r ON ms.RecipeID = r.RecipeID
                    WHERE ms.UserID = $userId
                    GROUP BY Date
                    HAVING WEEK(Date, 1) = $dateNum
                  ) AS ActualDates;
                  """
          case "week" =>
            SQL"""
                   SELECT CONCAT(Start_Date, ' - ', End_Date) AS Date_Name, Calories_Consumed
                   FROM (
                     SELECT DATE_ADD(Date, INTERVAL(0-WEEKDAY(Date)) DAY) AS Start_Date,
                       DATE_ADD(Date, INTERVAL(6-WEEKDAY(Date)) Day) AS End_Date,
                       SUM(Calories) AS Calories_Consumed FROM Meal_Slot ms
                     JOIN Recipe r ON ms.RecipeID = r.RecipeID
                     WHERE ms.UserID = $userId
                     GROUP BY Start_Date, End_Date
                   ) AS Start_End;
               """
          case "month" =>
            SQL"""
                 SELECT MONTHNAME(Date) AS Date_Name, SUM(Calories) AS Calories_Consumed
                 FROM Meal_Slot ms
                 JOIN Recipe r ON ms.RecipeID = r.RecipeID
                 WHERE ms.UserID = $userId
                 GROUP BY MONTHNAME(Date)
                 ORDER BY STR_TO_DATE(CONCAT('0001 ', Date_Name, ' 01'), '%Y %M %d');
                 """
          case "year" =>
            SQL"""
                 SELECT CONVERT(YEAR(Date), char) AS Date_Name, SUM(Calories) AS Calories_Consumed
                 FROM Meal_Slot ms
                 JOIN Recipe r ON ms.RecipeID = r.RecipeID
                 WHERE ms.UserID = $userId
                 GROUP BY CONVERT(YEAR(Date), char);
                 """
        }

        sqlQuery.as(metricsParser.*)
      }
    }(databaseExecutionContext)
  }

}
