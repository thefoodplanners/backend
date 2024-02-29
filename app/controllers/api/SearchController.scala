package controllers.api

import models.SearchDao
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext

/**
 * This controller handles http requests based on the search functionality.
 */
class SearchController @Inject()(
  cc: ControllerComponents,
  database: SearchDao,
)
  (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  /**
   * Fetch list of recipes based on search query.
   *
   * @param query Search query.
   * @return List of recipes.
   */
  def searchForRecipes(query: String): Action[AnyContent] = Action.async {
    database.searchForRecipes(query)
      .map(recipesWithImg => Ok(Json.toJson(recipesWithImg)))
  }

  /**
   * Fetch list of ingredients based on search query.
   *
   * @param query Search query.
   * @return List of ingredients.
   */
  def searchForIngredients(query: String): Action[AnyContent] = Action.async {
    database.searchForIngredients(query).map(ingredients => Ok(Json.toJson(ingredients)))
  }

}