package models

import anorm._
import org.joda.time.LocalDate
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

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
    SqlParser.int("Meal_SlotID") ~
    SqlParser.date("Date") ~
      SqlParser.int("Meal_Number") ~
      recipeParser
    ) map {
    case mealSlotId ~ date ~ mealNum ~ recipe =>
      FetchedMealSlot(mealSlotId, date, mealNum, recipe)
  }

  def fetchRecipe(recipeId: Int): Future[Recipe] = {
    Future {
      db.withConnection { implicit conn =>
        val allRows =
          SQL"""SELECT * FROM recipe
               WHERE RecipeID = $recipeId;""".as(recipeParser.single)
        allRows
      }
    }(databaseExecutionContext)
  }

  def fetchAllRecipes: Future[Seq[Recipe]] = {
    Future {
      db.withConnection { implicit conn =>
        val allRows = SQL"""SELECT * FROM recipe;""".as(recipeParser.*)
        allRows
      }
    }(databaseExecutionContext)
  }

  def addRecipe(recipe: Recipe): Future[Option[Long]] = {
    Future {
      db.withConnection { implicit conn =>
        val allRows =
          SQL"""INSERT INTO Recipe(Name, Type, Description, Time, Difficulty,
               Ingredients, Calories, Fats, Proteins, Carbohydrates, UserID,
               Vegan, Vegetarian, Keto, Lactose, Halal, Kosher, Dairy_free,
               Low_carbs, Gluten_free)
               VALUES (${recipe.name}, ${recipe.mealType}, ${recipe.desc}, ${recipe.time},
               ${recipe.difficulty}, ${recipe.ingredients}, ${recipe.calories}, ${recipe.fats},
               ${recipe.proteins}, ${recipe.carbohydrates}, ${recipe.preferences.isVegan});
             """.executeInsert()
        allRows
      }
    }(databaseExecutionContext)
  }

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

  def deleteMealSlot(userId: String, mealSlotId: Int): Future[Int] = {
    Future {
      db.withConnection { implicit conn =>
        SQL"""
              DELETE FROM Meal_Slot
              WHERE Meal_SlotID = $mealSlotId AND
              UserID = $userId;
             """.executeUpdate()
      }
    }(databaseExecutionContext)
  }

  def updateMealSlot(userId: String, mealSlotId: Int, newRecipeId: Int): Future[Int] = {
    Future {
      db.withConnection { implicit conn =>
        SQL"""
            UPDATE Meal_Slot
            SET RecipeID = $newRecipeId
            WHERE Meal_SlotID = $mealSlotId AND
            UserID = $userId;
           """.executeUpdate()
      }
    }(databaseExecutionContext)
  }

  def fetchAllMealSlots(userId: String, weekDateString: String): Future[Seq[FetchedMealSlot]] = {
    Future {
      db.withConnection { implicit conn =>
        val weekDate: LocalDate = LocalDate.parse(weekDateString)

        val allMealSlotRows: Seq[FetchedMealSlot] = {
          SQL"""
                SELECT *
                FROM Meal_Slot ms
                INNER JOIN Recipe r ON ms.RecipeId = r.RecipeId
                WHERE ms.UserID = $userId
                AND Date BETWEEN ${weekDate.toString} AND ${weekDate.withDayOfWeek(7).toString};
                """.as(mealSlotParser.*)
        }
        allMealSlotRows
      }
    }(databaseExecutionContext)
  }

  /**
   * Fetch all the recipes from the database that correspond with the preference
   * of the user.
   *
   * @param userId Id of the user requesting the query.
   * @return List of all the recipes from the database that correspond with the preference
   *         of the user
   */
  def fetchRecommendations(userId: Int): Future[Seq[Recipe]] = {
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
