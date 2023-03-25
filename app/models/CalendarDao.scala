package models

import anorm._
import org.joda.time.LocalDate
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

/**
 * DAO class for accessing the SQL database relating to calendar queries.
 */
class CalendarDao @Inject()(db: Database)(databaseExecutionContext: DatabaseExecutionContext) {

  private val recipeParser = (
    SqlParser.int("recipeId") ~
      SqlParser.str("name") ~
      SqlParser.str("type") ~
      SqlParser.str("description") ~
      SqlParser.int("time") ~
      SqlParser.str("difficulty") ~
      SqlParser.str("ingredients") ~
      SqlParser.int("calories") ~
      SqlParser.float("fats") ~
      SqlParser.float("proteins") ~
      SqlParser.float("carbohydrates") ~
      SqlParser.bool("vegan") ~
      SqlParser.bool("vegetarian") ~
      SqlParser.bool("keto") ~
      SqlParser.bool("lactose") ~
      SqlParser.bool("halal") ~
      SqlParser.bool("kosher") ~
      SqlParser.bool("dairy_free") ~
      SqlParser.bool("low_carbs") ~
      SqlParser.str("image")
    ) map {
    case id ~ name ~ mealType ~ desc ~ time ~ diff ~ ingr ~ cal ~ fats ~ proteins ~
      carbs ~ isVegan ~ isVegetarian ~ isKeto ~ isLactose ~ isHalal ~ isKosher ~
      isDairyFree ~ isLowCarbs ~ imageRef =>
      val preferences = Preferences(isVegan, isVegetarian, isKeto, isLactose, isHalal, isKosher, isDairyFree, isLowCarbs)
      Recipe(id, name, mealType, desc, time, diff, ingr, cal, fats, proteins, carbs, preferences, imageRef)
  }

  private val mealSlotParser = (
    SqlParser.int("TimetableID") ~
      SqlParser.date("Date") ~
      SqlParser.int("Meal_Number") ~
      recipeParser
    ) map {
    case mealSlotId ~ date ~ mealNum ~ recipe =>
      FetchedMealSlot(mealSlotId, date, mealNum, recipe)
  }

  /**
   * Adds the given meal slot to the database.
   *
   * @param userId   Id of the user.
   * @param mealSlot Meal slot to be added.
   * @return The generated primary key of the added meal slot.
   */
  def addMealSlot(userId: String, mealSlot: ReceivedMealSlot): Future[Option[Long]] = {
    Future {
      db.withConnection { implicit conn =>
        SQL"""
                INSERT INTO Meal_Slot(Date, Meal_Number, RecipeID, UserID)
                VALUES (${mealSlot.date}, ${mealSlot.mealNum}, ${mealSlot.recipeId}, $userId);
             """.executeInsert()
      }
    }(databaseExecutionContext)
  }

  /**
   * Deletes the meal slot from the database given the meal slot id.
   *
   * @param userId     Id of the user.
   * @param mealSlotId Id of the meal slot.
   * @return How many rows (meal slots) were affected by the deletion in the database. Should be 1.
   */
  def deleteMealSlot(userId: String, mealSlotId: Int): Future[Int] = {
    Future {
      db.withConnection { implicit conn =>
        SQL"""
              DELETE FROM Meal_Slot
              WHERE TimetableID = $mealSlotId AND
              UserID = $userId;
             """.executeUpdate()
      }
    }(databaseExecutionContext)
  }

  /**
   * Updates the meal slot in the database given the meal slot id with a new recipe id.
   *
   * @param userId      Id of the user.
   * @param mealSlotId  Id of the meal slot to be updated.
   * @param newRecipeId Id of the new recipe to be added.
   * @return How may rows (meal slots) were affected by the update in the database. Should be 1.
   */
  def updateMealSlot(userId: String, mealSlotId: Int, newRecipeId: Int): Future[Int] = {
    Future {
      db.withConnection { implicit conn =>
        SQL"""
            UPDATE Meal_Slot
            SET RecipeID = $newRecipeId
            WHERE TimetableID = $mealSlotId AND
            UserID = $userId;
           """.executeUpdate()
      }
    }(databaseExecutionContext)
  }

  /**
   * Fetches all the meal slots for a given week and given user.
   *
   * @param userId   The id of the user.
   * @param weekDate Date corresponding to the Monday of the given week.
   * @return List of all the meal slots for that given week and given user.
   */
  def fetchAllMealSlots(userId: String, weekDate: String): Future[Seq[FetchedMealSlot]] = {
    Future {
      db.withConnection { implicit conn =>
        val weekLocalDate: LocalDate = LocalDate.parse(weekDate)
        SQL"""
              SELECT *
              FROM Meal_Slot ms
              INNER JOIN Recipe r ON ms.RecipeId = r.RecipeId
              WHERE ms.UserID = $userId
              AND Date BETWEEN ${weekLocalDate.toString} AND ${weekLocalDate.withDayOfWeek(7).toString};
              """.as(mealSlotParser.*)
      }
    }(databaseExecutionContext)
  }

  /**
   * Fetch all the recipes from the database that correspond with the preference
   * of the user.
   *
   * @param userId Id of the user.
   * @return List of all the recipes from the database that correspond with the preference
   *         of the user
   */
  def fetchRecommendations(userId: String): Future[Seq[Recipe]] = {
    Future {
      db.withConnection { implicit conn =>
        SQL"""
              SELECT RecipeID, Name, Type, Description, Time, Difficulty, Ingredients, Calories,
                Fats, Proteins, Carbohydrates, R.Vegan, R.Vegetarian, R.Keto, R.Lactose, R.Halal,
                R.Kosher, R.Dairy_free, R.Low_carbs, image
              FROM Recipe R
              INNER JOIN Preferences P ON
                P.UserID = $userId AND
                (R.Vegan OR (NOT P.Vegan)) AND
                (R.Vegetarian OR (NOT P.Vegetarian)) AND
                (R.Keto OR (NOT P.Keto)) AND
                (R.Lactose OR (NOT P.Lactose)) AND
                (R.Halal OR (NOT P.Halal)) AND
                (R.Kosher OR (NOT P.Kosher)) AND
                (R.Dairy_free OR (NOT P.Dairy_free)) AND
                (R.Low_carbs OR (NOT P.Low_carbs))
              ORDER BY RAND();
             """.as(recipeParser.*)
      }
    }(databaseExecutionContext)
  }

}
