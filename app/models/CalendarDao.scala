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
class CalendarDao @Inject()(db: Database, loginDao: LoginDao)(databaseExecutionContext: DatabaseExecutionContext) {

  val preferencesParser: RowParser[Preferences] = (
    SqlParser.bool("Vegan") ~
      SqlParser.bool("Vegetarian") ~
      SqlParser.bool("Keto") ~
      SqlParser.bool("Lactose") ~
      SqlParser.bool("Halal") ~
      SqlParser.bool("Kosher") ~
      SqlParser.bool("Dairy_Free") ~
      SqlParser.bool("Low_Carbs") ~
      SqlParser.bool("Gluten_Free") ~
      SqlParser.bool("Peanuts") ~
      SqlParser.bool("Eggs") ~
      SqlParser.bool("Fish") ~
      SqlParser.bool("Tree_Nuts") ~
      SqlParser.bool("Soy")
    ) map {
    case isVegan ~ isVegetarian ~ isKeto ~ isLactose ~ isHalal ~ isKosher ~
      isDairyFree ~ isLowCarbs ~ isGlutenFree ~ isPeanuts ~ isEggs ~ isFish ~
      isTreeNuts ~ isSoy =>
      Preferences(isVegan, isVegetarian, isKeto, isLactose, isHalal,
        isKosher, isDairyFree, isLowCarbs, isGlutenFree, isPeanuts, isEggs, isFish, isTreeNuts, isSoy, None
      )
  }

  val recipeParser: RowParser[Recipe] = (
    SqlParser.int("RecipeID") ~
      SqlParser.str("Name") ~
      SqlParser.str("Type") ~
      SqlParser.str("Description") ~
      SqlParser.int("Time") ~
      SqlParser.str("Difficulty") ~
      SqlParser.str("Ingredients") ~
      SqlParser.int("Calories") ~
      SqlParser.float("Fats") ~
      SqlParser.float("Proteins") ~
      SqlParser.float("Carbohydrates") ~
      SqlParser.str("image") ~
      preferencesParser
    ) map {
    case id ~ name ~ mealType ~ desc ~ time ~ diff ~ ingr ~ cal ~ fats ~ proteins ~
      carbs ~ imageRef ~ preferences =>
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

  private val movedMealSlotParser = (
    SqlParser.date("Date") ~
      SqlParser.int("Meal_Number")
  ) map {
    case date ~ mealNum =>
      MovedMealSlot(0, date, mealNum)
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
        val nextMealNum: Int =
          SQL"""
                 SELECT COALESCE(MAX(Meal_Number), 0) FROM Meal_Slot
                 WHERE Date = ${mealSlot.date} AND
                 UserID = $userId;
                 """.as(SqlParser.scalar[Int].single)

        SQL"""
              INSERT INTO Meal_Slot(Date, Meal_Number, RecipeID, UserID)
              VALUES (${mealSlot.date}, ${nextMealNum + 1}, ${mealSlot.recipeId}, $userId);
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

  def moveMealSlot(userId: String, newMovedMealSlot: MovedMealSlot): Future[Int] = {
    Future {
      db.withConnection { implicit conn =>
        val oldMovedMealSlot =
          SQL"""
                SELECT Date, Meal_Number
                FROM Meal_Slot
                WHERE TimetableID = ${newMovedMealSlot.mealSlotId} AND
                UserID = $userId;
                """.as(movedMealSlotParser.single)

        val oldMeals =
          SQL"""
               SELECT TimetableID
               FROM Meal_Slot
               WHERE Date = ${oldMovedMealSlot.date} AND
               Meal_Number >= ${oldMovedMealSlot.mealNum};
               """.as(SqlParser.scalar[Int].*)

        oldMeals.foreach { mealSlotId =>
          SQL"""
                        UPDATE Meal_Slot
                        SET Meal_Number = Meal_Number - 1
                        WHERE TimetableID = $mealSlotId
                        """.execute()
        }

        val newMeals =
          SQL"""
                SELECT TimetableID
                FROM Meal_Slot
                WHERE Date = ${newMovedMealSlot.date} AND
                Meal_Number >= ${newMovedMealSlot.mealNum};
                """.as(SqlParser.scalar[Int].*)

        newMeals.foreach { mealSlotId =>
          SQL"""
                UPDATE Meal_Slot
                SET Meal_Number = Meal_Number + 1
                WHERE TimetableID = $mealSlotId
                """.execute()
        }

        SQL"""
              UPDATE Meal_Slot
              SET Date = ${newMovedMealSlot.date}, Meal_Number = ${newMovedMealSlot.mealNum}
              WHERE TimetableID = ${newMovedMealSlot.mealSlotId};
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
  def fetchRecommendations(userId: String, dateOpt: Option[String]): Future[Seq[Recipe]] = {
    Future {
      db.withConnection { implicit conn =>

        val addQuery = dateOpt.map { date =>
          val targetCalories =
            SQL"""
                  SELECT Target_Calories
                  FROM Preferences
                  WHERE UserID = $userId;
                  """.as(SqlParser.scalar[Int].single)

          val totalCaloriesForDay =
            SQL"""
                  SELECT SUM(Calories) AS Total_Calories
                  FROM Meal_Slot ms
                  INNER JOIN Recipe r ON
                    ms.RecipeID = r.RecipeID AND
                    Date = $date
               """.as(SqlParser.scalar[Int].single)

          val caloriesLeft = targetCalories - totalCaloriesForDay

          s"AND Calories <= '$caloriesLeft'"
        }.getOrElse("")

        SQL"""
              SELECT RecipeID, Name, Type, Description, Time, Difficulty, Ingredients, Calories,
                Fats, Proteins, Carbohydrates, R.Vegan, R.Vegetarian, R.Keto, R.Lactose, R.Halal,
                R.Kosher, R.Dairy_free, R.Low_carbs, R.Gluten_free, R.Peanuts, R.Eggs, R.Fish,
                R.Tree_nuts, R.Soy, image
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
                (R.Low_carbs OR (NOT P.Low_carbs)) AND
                (R.Gluten_free OR (NOT P.Gluten_free)) AND
                (R.Peanuts OR (NOT P.Peanuts)) AND
                (R.Eggs OR (NOT P.Eggs)) AND
                (R.Fish OR (NOT P.Fish)) AND
                (R.Tree_nuts OR (NOT P.Tree_nuts)) AND
                (R.Soy OR (NOT P.Soy))
                #$addQuery
              ORDER BY RAND();
             """.as(recipeParser.*)
      }
    }(databaseExecutionContext)
  }

  def storeGeneratedMealPlan(userId: String, date: LocalDate, mealSlots: Seq[FetchedMealSlot]): Future[Boolean] = {
    Future {
      db.withConnection { implicit conn =>
        SQL"""
              DELETE FROM Meal_Slot
              WHERE Date BETWEEN ${date.toString} AND ${date.withDayOfWeek(7).toString}
              AND UserID = $userId;
           """.execute()

        mealSlots.map { mealSlot =>
          SQL"""
                INSERT INTO Meal_Slot(Date, Meal_Number, RecipeID, UserID)
                VALUES(${mealSlot.date}, ${mealSlot.mealNum}, ${mealSlot.recipe.id}, $userId);
             """.execute()
        }.forall(_ == true)
      }
    }(databaseExecutionContext)
  }
}
