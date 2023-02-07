package controllers.api

import models.{ FetchedMealSlot, Recipe, RecipeDao, SESSION_KEY }
import org.joda.time.LocalDate
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }

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

  def addRecipe: Action[JsValue] = Action.async(parse.json) { request =>
    val recipe: Option[Recipe] = Json.fromJson[Recipe](request.body).asOpt
    ???
  }

  def getRecipeFromId(recipeId: Int): Action[AnyContent] = Action.async {
    val recipeJsonString = database.fetchRecipe(recipeId).map { recipe =>
      Json.stringify(Json.obj("recipe" -> recipe))
    }
    recipeJsonString.map(Ok(_))
  }

  /**
   * Action which fetches all the meal slots for a given user in a given week.
   *
   * @param weekDateString The week in string format. E.g. "2023-W03".
   * @return HTTP response with JSON consisting of 2D array of all meal slots.
   */
  def getAllMealSlots(weekDateString: String): Action[AnyContent] = Action.async { request =>
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        val mealSlotsJsonString = database.fetchAllMealSlots(userId.toInt, weekDateString).map { mealSlots =>
          val mealSlotsArray = mealSlotToArray(mealSlots)
          Json.stringify(Json.toJson(mealSlotsArray))
        }
        mealSlotsJsonString.map(Ok(_))
      }
      .getOrElse {
        Future.successful(Unauthorized("Sorry buddy, not allowed in"))
      }
  }

  /**
   * Converts list of FetchedMealSlot case class to a 2d array, with the number of days
   * of the week being the column size and the number of meal numbers being the row size.
   *
   * @param mealSlots List of FetchedMealSlot case class objects.
   * @return A 2D array of mealSlots, with either empty items or a recipe object.
   */
  private def mealSlotToArray(mealSlots: Seq[FetchedMealSlot]): Seq[Seq[Recipe]] = {
    mealSlots
      .groupBy { fetchedMealSlot =>
        val dayOfWeekNum = LocalDate.fromDateFields(fetchedMealSlot.date).getDayOfWeek
        dayOfWeekNum - 1
      }
      .toSeq
      .map(_._2.map(_.recipe))
  }

}
