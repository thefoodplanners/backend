package models

import anorm._
import org.joda.time.LocalDate
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

class RecipeDao @Inject()(db: Database)(databaseExecutionContext: DatabaseExecutionContext) {

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
      SqlParser.bool("low_carbs")
    ) map {
    case id ~ name ~ mealType ~ desc ~ time ~ diff ~ ingr ~ cal ~ fats ~ proteins ~
      carbs ~ isVegan ~ isVegetarian ~ isKeto ~ isLactose ~ isHalal ~ isKosher ~
      isDairyFree ~ isLowCarbs =>
      val preferences = Preferences(isVegan, isVegetarian, isKeto, isLactose, isHalal, isKosher, isDairyFree, isLowCarbs)
      Recipe(id, name, mealType, desc, time, diff, ingr, cal, fats, proteins, carbs, preferences)
  }

  private val mealSlotParser = (
    SqlParser.date("Date") ~
      SqlParser.int("Meal_Number") ~
      recipeParser
    ) map {
    case date ~ mealNum ~ recipe =>
      FetchedMealSlot(date, mealNum, recipe)
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

  def addMealToSlot(slot: ReceivedMealSlot): Future[Option[Long]] = {
    Future {
      db.withConnection { implicit conn =>
        val newRow =
          SQL"""INSERT INTO Meal_Slot(Date, Type, RecipeID, UserID)
               VALUES (${slot.date}, ${slot.mealType}, ${slot.recipeId}, ${slot.userId});
             """.executeInsert()
        newRow
      }
    }(databaseExecutionContext)
  }

  def fetchAllMealSlots(userId: Int, weekDateString: String): Future[Seq[FetchedMealSlot]] = {
    Future {
      db.withConnection { implicit conn =>
        val weekDate: LocalDate = LocalDate.parse(weekDateString)

        val allRows: Seq[FetchedMealSlot] = {
          SQL"""SELECT *
               FROM Meal_Slot ms
               INNER JOIN Recipe r ON ms.RecipeId = r.RecipeId
               WHERE ms.UserID = $userId
               AND Date BETWEEN ${weekDate.toString} AND ${weekDate.withDayOfWeek(7).toString};
               """.as(mealSlotParser.*)
        }
        allRows
      }
    }(databaseExecutionContext)
  }

  /*
  Recommendation.
  Fetch preferences of user and which meal type they picked (breakfast, lunch, dinner)
  fetch all recipes which fit that preference and meal type
  randomise order
  display first n recipes. Queue could work.
  Refreshing:
  if user refreshes, next n recipes will be displayed and taken out of the queue.
  Once queue is empty, could either repeat recommendation steps above or fetch all recipes
  with less constrained preferences.
   */
  def fetchRecommendations(userId: Int): Future[List[Recipe]] = {
    Future {
      db.withConnection { implicit conn =>
        val allRows =
          SQL"""SELECT *
               FROM Recipe R
               INNER JOIN Users U
                 ON (R.Vegan = U.Vegan
                   AND R.Vegetarian = U.Vegetarian
                   AND R.Keto = U.Keto
                   AND R.Lactose = U.Lactose
                   AND R.Halal = U.Halal
                   AND R.Kosher = U.Kosher
                   AND R.Dairy_free = U.Dairy_free
                   AND R.Low_carbs = U.Low_carbs)
               """.as(recipeParser.*)
        allRows
      }
    }(databaseExecutionContext)
  }

}
