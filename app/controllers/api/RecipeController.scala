package controllers.api

import models.LoginDao
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class RecipeController @Inject()(cc: ControllerComponents, database: LoginDao)
  (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def getAllRecipes: Action[AnyContent] = Action.async {
    val recipesJsonString = database.fetchAllRecipes.map { recipes =>
      Json.stringify(Json.obj("recipes" -> recipes))
    }
    recipesJsonString.map(Ok(_))
  }
}
