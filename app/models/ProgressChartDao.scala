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
      SqlParser.int("Total_Cals") ~
      SqlParser.double("Total_Fats") ~
      SqlParser.double("Total_Proteins") ~
      SqlParser.double("Total_Carbs")
    ) map {
    case date ~ totalCalories ~ totalFats ~ totalProteins ~ totalCarbs =>
      Metrics(date, totalCalories, totalFats, totalProteins, totalCarbs)
  }

  def fetchMetrics(userId: String, dateType: String, date: String): Future[MetricsWithLabel] = {
    Future {
      db.withConnection { implicit conn =>
        val localDate = LocalDate.parse(date)

        val sqlQuery = dateType match {
          case "day" =>
              SQL"""
                  SELECT
                    CONCAT('Meal ', Meal_Number) AS Date_Name,
                    Calories AS Total_Cals,
                    ROUND(Fats, 1) AS Total_Fats,
                    ROUND(Proteins, 1) AS Total_Proteins,
                    ROUND(Carbohydrates, 1) AS Total_Carbs
                  FROM Meal_Slot ms INNER JOIN Recipe r ON ms.RecipeID =r.RecipeID
                  WHERE ms.UserID = $userId AND
                  Date=$date
                  ORDER BY Meal_Number;
               """
          case "week" =>
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
                      ROUND(SUM(Fats), 1) AS Total_Fats,
                      ROUND(SUM(Proteins), 1) AS Total_Proteins,
                      ROUND(SUM(Carbohydrates), 1) AS Total_Carbs
                    FROM Meal_Slot ms
                    JOIN Recipe r ON ms.RecipeID = r.RecipeID
                    WHERE ms.UserID = $userId
                    GROUP BY Date
                    HAVING WEEK(Date, 1) = ${localDate.getWeekOfWeekyear}
                  ) AS ActualDates;
                  """
          case "month" =>
            SQL"""
                   SELECT
                     CONCAT(DATE_FORMAT(Start_Date, '%b %d'), ' - ', DATE_FORMAT(End_Date, '%b %d')) AS Date_Name,
                     Total_Cals,
                     Total_Fats,
                     Total_Proteins,
                     Total_Carbs
                   FROM (
                     SELECT
                       DATE_ADD(Date, INTERVAL(0-WEEKDAY(Date)) Day) AS Start_Date,
                       DATE_ADD(Date, INTERVAL(6-WEEKDAY(Date)) Day) AS End_Date,
                       SUM(Calories) AS Total_Cals,
                       ROUND(SUM(Fats), 1) AS Total_Fats,
                       ROUND(SUM(Proteins), 1) AS Total_Proteins,
                       ROUND(SUM(Carbohydrates), 1) AS Total_Carbs
                     FROM Meal_Slot ms
                     JOIN Recipe r ON ms.RecipeID = r.RecipeID
                     WHERE ms.UserID = $userId AND
                     MONTH(Date) = ${localDate.getMonthOfYear}
                     GROUP BY Start_Date, End_Date
                   ) AS Start_End;
               """
          case "year" =>
            SQL"""
                 SELECT
                   MONTHNAME(Date) AS Date_Name,
                   SUM(Calories) AS Total_Cals,
                   ROUND(SUM(Fats), 1) AS Total_Fats,
                   ROUND(SUM(Proteins), 1) AS Total_Proteins,
                   ROUND(SUM(Carbohydrates), 1) AS Total_Carbs
                 FROM Meal_Slot ms
                 JOIN Recipe r ON ms.RecipeID = r.RecipeID
                 WHERE ms.UserID = $userId AND
                 YEAR(Date) = ${localDate.getYear}
                 GROUP BY MONTHNAME(Date)
                 ORDER BY STR_TO_DATE(CONCAT('0001 ', Date_Name, ' 01'), '%Y %M %d');
                 """
        }

        val metrics = sqlQuery.as(metricsParser.*)

        val label: String = dateType match {
          case "day" => localDate.toString("EEE (MMM dd yyyy)")
          case "week" => s"${localDate.toString("MMM dd yyyy")} - ${localDate.plusDays(6).toString("MMM dd yyyy")}"
          case "month" => localDate.toString("MMMM yyyy")
          case "year" => localDate.getYear.toString
        }

        MetricsWithLabel(date, label, metrics)
      }
    }(databaseExecutionContext)
  }

}
