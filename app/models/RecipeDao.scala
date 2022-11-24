package models

import anorm._
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

class RecipeDao @Inject()(db: Database)(databaseExecutionContext: DatabaseExecutionContext) {

  def fetchAllRecipes: Future[List[Recipe]] = {
    Future {
      db.withConnection { implicit conn =>
        val recipeParser = (
          SqlParser.str("name") ~
            SqlParser.str("type") ~
            SqlParser.str("description") ~
            SqlParser.str("time") ~
            SqlParser.str("difficulty") ~
            SqlParser.str("ingredients") ~
            SqlParser.str("instructions") ~
            SqlParser.str("calories") ~
            SqlParser.str("fats")
          ) map {
          case name ~ mealType ~ desc ~ time ~ diff ~ ingr ~ instr ~ cal ~ fats =>
            Recipe(name, mealType, desc, time, diff, ingr, instr, cal, fats)
        }

        val allRows = SQL"""SELECT * FROM recipe;""".as(recipeParser.*)
        allRows
      }
    } (databaseExecutionContext)
  }

  def addRecipe(recipe: Recipe): Future[Boolean] = {
    Future {
      db.withConnection { implicit conn =>

        val allRows = SQL"""SELECT * FROM recipe;""".executeInsert()
        allRows
      }
    } (databaseExecutionContext)
  }
}
