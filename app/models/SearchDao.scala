package models

import anorm._
import org.joda.time.LocalDate
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

class SearchDao @Inject()(db: Database)(databaseExecutionContext: DatabaseExecutionContext) {

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

  def searchForRecipes(query: String): Future[Seq[Recipe]] = {
    Future {
      db.withConnection { implicit conn =>
        val queryWithWildcard: String = s"%$query%"
        SQL"""
             SELECT * FROM Recipe
             WHERE Name LIKE $queryWithWildcard;
             """.as(recipeParser.*)
      }
    }(databaseExecutionContext)
  }

}
