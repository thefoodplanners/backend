package models

import anorm._
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

class RecipeDao @Inject()(db: Database)(databaseExecutionContext: DatabaseExecutionContext) {

  private val recipeParser = (
    SqlParser.int("id") ~
      SqlParser.str("name") ~
      SqlParser.str("type") ~
      SqlParser.str("description") ~
      SqlParser.int("time") ~
      SqlParser.str("difficulty") ~
      SqlParser.str("ingredients") ~
      SqlParser.str("instructions") ~
      SqlParser.int("calories") ~
      SqlParser.int("fats") ~
      SqlParser.int("proteins") ~
      SqlParser.int("carbohydrates") ~
      SqlParser.bool("vegan") ~
      SqlParser.bool("vegetarian") ~
      SqlParser.bool("keto") ~
      SqlParser.bool("lactose") ~
      SqlParser.bool("halal") ~
      SqlParser.bool("kosher") ~
      SqlParser.bool("dairy_free") ~
      SqlParser.bool("low_carbs")
    ) map {
    case id ~ name ~ mealType ~ desc ~ time ~ diff ~ ingr ~ instr ~ cal ~ fats ~ proteins ~
      carbs ~ isVegan ~ isVegetarian ~ isKeto ~ isLactose ~ isHalal ~ isKosher ~
      isDairyFree ~ isLowCarbs =>
      val preferences = Preferences(isVegan, isVegetarian, isKeto, isLactose, isHalal, isKosher, isDairyFree, isLowCarbs)
      Recipe(id, name, mealType, desc, time, diff, ingr, instr, cal, fats, proteins, carbs, preferences)
  }

  def fetchAllRecipes: Future[List[Recipe]] = {
    Future {
      db.withConnection { implicit conn =>
        val allRows = SQL"""SELECT * FROM recipe;""".as(recipeParser.*)
        allRows
      }
    } (databaseExecutionContext)
  }

  def addRecipe(recipe: Recipe): Future[Option[Long]] = {
    Future {
      db.withConnection { implicit conn =>

        val allRows = SQL"""SELECT * FROM recipe;""".executeInsert()
        allRows
      }
    } (databaseExecutionContext)
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
