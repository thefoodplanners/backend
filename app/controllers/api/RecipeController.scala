package controllers.api

import models.{ Recipe, RecipeDao }
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext

/**
 * This controller handles HTTP requests dealing
 * with recipes.
 */
@Singleton
class RecipeController @Inject()(cc: ControllerComponents, database: RecipeDao)
  (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def getAllRecipes: Action[AnyContent] = Action.async {
    val recipesJsonString = database.fetchAllRecipes.map { recipes =>
      Json.stringify(Json.obj("recipes" -> recipes))
    }
    recipesJsonString.map(Ok(_))
  }

  def addRecipe: Action[AnyContent] = Action.async { request =>
    val jsonObj: Option[JsValue] = request.body.asJson
    val recipe: Option[Recipe] = jsonObj.flatMap(Json.fromJson[Recipe](_).asOpt)
    ???
  }

}
