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
      SqlParser.int("Total_Cals")~
      SqlParser.double("Total_Fats") ~
      SqlParser.double("Total_Proteins") ~
      SqlParser.double("Total_Carbs")
  ) map {
    case date ~ totalCalories ~ totalFats ~ totalProteins ~ totalCarbs =>
      Metrics(date, totalCalories, totalFats, totalProteins, totalCarbs)
  }

  def fetchMetrics(userId: String, dateType: String, date: String): Future[Seq[Metrics]] = {
    Future {
      db.withConnection { implicit conn =>
        val localDate = LocalDate.parse(date)
        val range = 7

        val sqlQuery = dateType match {
          case "day" =>
            SQL"""
                  SELECT
                    DAYNAME(Date) AS Date_Name,
                    Total_Cals,
                    Total_Fats,
                    Total_Proteins,
                    Total_Carbs
                  FROM (
                    SELECT
                      Date,
                      SUM(Calories) AS Total_Cals,
                      SUM(Fats) AS Total_Fats,
                      SUM(Proteins) AS Total_Proteins,
                      SUM(Carbohydrates) AS Total_Carbs
                    FROM Meal_Slot ms
                    JOIN Recipe r ON ms.RecipeID = r.RecipeID
                    WHERE ms.UserID = $userId
                    GROUP BY Date
                    HAVING WEEK(Date, 1) = ${localDate.getWeekOfWeekyear}
                  ) AS ActualDates;
                  """
          case "week" =>
            SQL"""
                   SELECT
                     CONCAT(DATE_FORMAT(Start_Date, '%b %d'), ' - ', DATE_FORMAT(End_Date, '%b %d')) AS Date_Name,
                     Total_Cals,
                     Total_Fats,
                     Total_Proteins,
                     Total_Carbs
                   FROM (
                     SELECT
                       DATE_ADD(Date, INTERVAL(0-WEEKDAY(Date)) DAY) AS Start_Date,
                       DATE_ADD(Date, INTERVAL(6-WEEKDAY(Date)) Day) AS End_Date,
                       SUM(Calories) AS Total_Cals,
                       SUM(Fats) AS Total_Fats,
                       SUM(Proteins) AS Total_Proteins,
                       SUM(Carbohydrates) AS Total_Carbs
                     FROM Meal_Slot ms
                     JOIN Recipe r ON ms.RecipeID = r.RecipeID
                     WHERE ms.UserID = $userId AND
                     Date BETWEEN ${localDate.minusWeeks(range).toString} AND ${localDate.plusWeeks(range).toString}
                     GROUP BY Start_Date, End_Date
                   ) AS Start_End;
               """
          case "month" =>
            SQL"""
                 SELECT
                   MONTHNAME(Date) AS Date_Name,
                   SUM(Calories) AS Total_Cals,
                   SUM(Fats) AS Total_Fats,
                   SUM(Proteins) AS Total_Proteins,
                   SUM(Carbohydrates) AS Total_Carbs
                 FROM Meal_Slot ms
                 JOIN Recipe r ON ms.RecipeID = r.RecipeID
                 WHERE ms.UserID = $userId AND
                 Date BETWEEN ${localDate.minusMonths(range).toString} AND ${localDate.plusMonths(range).toString}
                 GROUP BY MONTHNAME(Date)
                 ORDER BY STR_TO_DATE(CONCAT('0001 ', Date_Name, ' 01'), '%Y %M %d');
                 """
          case "year" =>
            SQL"""
                 SELECT
                   CONVERT(YEAR(Date), char) AS Date_Name,
                   SUM(Calories) AS Total_Cals,
                   SUM(Fats) AS Total_Fats,
                   SUM(Proteins) AS Total_Proteins,
                   SUM(Carbohydrates) AS Total_Carbs
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
