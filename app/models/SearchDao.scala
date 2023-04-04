package models

import anorm._
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

/**
 * DAO class for accessing the SQL database relating to search queries.
 */
class SearchDao @Inject()(
  db: Database,
  calendarDao: CalendarDao
)
  (databaseExecutionContext: DatabaseExecutionContext) {

  private val ingredientsParser = (
    SqlParser.int("IngredientID") ~
      SqlParser.str("Name") ~
      SqlParser.int("Calories") ~
      SqlParser.double("Fats") ~
      SqlParser.double("Proteins") ~
      SqlParser.double("Carbohydrates")
  ) map {
    case ingredientId ~ name ~ calories ~ fats ~ proteins ~ carbs =>
      Ingredients(ingredientId, name, calories, fats, proteins, carbs)
  }

  /**
   * Fetches list of recipes with satisfy query.
   * @param query Query string.
   * @return List of recipes.
   */
  def searchForRecipes(query: String): Future[Seq[Recipe]] = {
    Future {
      db.withConnection { implicit conn =>
        val queryWithWildcard: String = s"%$query%"
        SQL"""
             SELECT * FROM Recipe
             WHERE Name LIKE $queryWithWildcard;
             """.as(calendarDao.recipeParser.*)
      }
    }(databaseExecutionContext)
  }

  /**
   * Fetches list of ingredients with satisfy query.
   * @param query Query string.
   * @return List of ingredients.
   */
  def searchForIngredients(query: String): Future[Seq[Ingredients]] = {
    Future {
      db.withConnection { implicit conn =>
        val queryWithWildcard: String = s"%$query%"
        SQL"""
           SELECT * FROM Ingredients
           WHERE Name LIKE $queryWithWildcard;
           """.as(ingredientsParser.*)
      }
    }(databaseExecutionContext)
  }
}
