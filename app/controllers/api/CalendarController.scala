package controllers.api

import controllers.customActions.AuthenticatedUserAction
import models._
import org.joda.time.LocalDate
import play.api.libs.json.{ JsError, JsValue, Json }
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random

/**
 * This controller handles HTTP requests dealing with the calendar.
 */
@Singleton
class CalendarController @Inject()(
  cc: ControllerComponents,
  database: CalendarDao,
  databaseUser: UserDao
)
  (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  /**
   * Action which fetches all the recommended recipes for a given user.
   *
   * @return HTTP response consisting of 2D array of all the recommended
   *         recipes, split into groups of 3.
   */
  def getRecommendations(
    date: String,
    fats: Option[Double],
    proteins: Option[Double],
    carbohydrates: Option[Double]
  ): Action[AnyContent] = Action.async { request =>
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        for {
          recipes <- database.fetchRecommendations(userId, Some(date), fats, proteins, carbohydrates)
          splitRecipes = recipes.grouped(3).toSeq
        } yield Ok(Json.toJson(splitRecipes))
      }
      .getOrElse {
        Future.successful(Unauthorized(UNAUTH_MSG))
      }
  }

  /**
   * Generate a week's worth of meals for a given week.
   *
   * @param date Week to be generated for.
   * @return Ok response stating that generated meals are now in the database.
   */
  def generateWeeklyMealPlan(date: String): Action[AnyContent] = Action.async { request =>
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        for {
          maxCalories <- databaseUser.fetchTargetCalories(userId)
          recipes <- database.fetchRecommendations(userId, None, None, None, None)

          localDate = LocalDate.parse(date)

          recipesByMealType = recipes.groupBy(_.mealType)
          weeklyMeals = Seq.tabulate(7) { dayIndex =>
            val rand = new Random
            val pick = rand.nextInt(3)
            val dayMeal = pick match {
              case 0 => dayMealPlan(recipesByMealType, maxCalories, "Breakfast", rand)
              case 1 => dayMealPlan(recipesByMealType, maxCalories, "Lunch", rand)
              case 2 => dayMealPlan(recipesByMealType, maxCalories, "Dinner", rand)
            }
            dayMeal.zipWithIndex.map { case (recipe, mealNumIndex) =>
              FetchedMealSlot(0, localDate.plusDays(dayIndex).toDate, mealNumIndex + 1, recipe)
            }
          }.flatten

          _ <- database.storeGeneratedMealPlan(userId, localDate, weeklyMeals)
        } yield Ok
      }
      .getOrElse {
        Future.successful(Unauthorized(UNAUTH_MSG))
      }
  }

  /**
   * Generate a meal plan for a single day.
   *
   * @param recipesByMealType Maps between recipes and what their meal type is (breakfast, lunch, dinner).
   * @param maxCalories       Max amount of calories user can have for a day.
   * @param lastMealType      Picks whether breakfast, lunch or dinner is generated last.
   * @param rand              Random object.
   * @return List of recipes generated for the day.
   */
  private def dayMealPlan(
    recipesByMealType: Map[String, Seq[Recipe]],
    maxCalories: Int,
    lastMealType: String,
    rand: Random
  ): Seq[Recipe] = {
    val mealTypes = Seq("Breakfast", "Lunch", "Dinner")

    // Randomly pick first two recipes
    val randomRecipes = mealTypes
      .filterNot(_ == lastMealType)
      .map(mealType => getRandomElem(recipesByMealType(mealType), rand))

    val caloriesLeft = maxCalories - randomRecipes.map(_.calories).sum
    // Picks last recipe based on calories left that do not go over max calories limit
    val lastRecipe = getRandomElem(recipesByMealType(lastMealType).filter(_.calories <= caloriesLeft), rand)

    (randomRecipes :+ lastRecipe).sortBy { recipe =>
      recipe.mealType match {
        case "Breakfast" => 0
        case "Lunch" => 1
        case "Dinner" => 2
        case _ => 3
      }
    }
  }

  /**
   * Generic method for picking random element from list.
   * No built-in method for this.
   *
   * @param seq    List.
   * @param random Random object.
   * @tparam A Generic type.
   * @return Random element from list.
   */
  private def getRandomElem[A](seq: Seq[A], random: Random): A =
    seq(random.nextInt(seq.length))

  /**
   * Called when a meal slot is moved to another position in the calendar.
   * Updates database with this change in position.
   *
   * @return Response on whether the meal slot was successfully moved in the database.
   */
  def moveMealSlot: Action[JsValue] = Action.async(parse.json) { request =>
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        request.body.validate[MovedMealSlot].map { movedMealSlot =>
            database.moveMealSlot(userId, movedMealSlot).map { rowsUpdated =>
              if (rowsUpdated == 1) Ok("Meal slot successfully moved.")
              else if (rowsUpdated == 0) InternalServerError("Meal slot not moved.")
              else InternalServerError("Multiple meal slots moved.")
            }
          }
          .recoverTotal(error => Future.successful(BadRequest(
            Json.obj(
              "error" -> "JSON parse",
              "message" -> JsError.toJson(error)
            )
          )))

      }
      .getOrElse {
        Future.successful(Unauthorized(UNAUTH_MSG))
      }
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
        for {
          mealSlots <- database.fetchAllMealSlots(userId, weekDateString)
          mealSlotArray = mealSlotToArray(mealSlots)
        } yield Ok(Json.toJson(mealSlotArray))
      }
      .getOrElse {
        Future.successful(Unauthorized(UNAUTH_MSG))
      }
  }

  /**
   * Converts list of FetchedMealSlot case class to a 2d array, with the number of days
   * of the week being the column size and the number of meal numbers being the row size.
   *
   * @param mealSlots List of FetchedMealSlot case class objects.
   * @return A 2D array of mealSlots, with either empty items or a recipe object.
   */
  private def mealSlotToArray(mealSlots: Seq[FetchedMealSlot]): Seq[Seq[MealSlot]] = {
    val daySlots = mealSlots
      .groupBy { fetchedMealSlot =>
        val dayOfWeekNum = LocalDate.fromDateFields(fetchedMealSlot.date).getDayOfWeek
        dayOfWeekNum - 1
      }
      .map { case (dayIndex, fetchedMealSlots) =>
        (dayIndex, fetchedMealSlots.sortBy(_.mealNum))
      }

    Seq.tabulate(7) { index =>
      daySlots
        .get(index)
        .map(_.map(mealSlot => MealSlot(mealSlot.mealSlotId, mealSlot.recipe)))
        .getOrElse(Seq.empty)
    }
  }

  /**
   * Adds given meal slot to database.
   *
   * @return Response on whether meal slot was successfully added to database.
   */
  def addMealSlot(): Action[JsValue] = Action.async(parse.json) { request =>
    // Fetch user id from session cookie
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        // Convert request body json to case class
        Json.fromJson[ReceivedMealSlot](request.body)
          .asOpt
          .map { mealSlot =>
            database
              .addMealSlot(userId, mealSlot)
              .map(primaryKeyOpt =>
                primaryKeyOpt
                  .map(_ => Ok("Meal successfully added."))
                  .getOrElse {
                    InternalServerError("Could not add meal slot to database.")
                  }
              )
          }
          .getOrElse {
            Future.successful(BadRequest("Error in processing Json data in request body."))
          }
      }
      .getOrElse {
        Future.successful(Unauthorized(UNAUTH_MSG))
      }
  }

  /**
   * Deletes a given meal slot from the database.
   *
   * @param mealSlotId Id of the meal slot to be deleted.
   * @return Response on whether meal slot was successfully deleted.
   */
  def deleteMealSlot(mealSlotId: Int): Action[AnyContent] = Action.async { request =>
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        database
          .deleteMealSlot(userId, mealSlotId)
          .map { rowsAffected =>
            if (rowsAffected == 0) InternalServerError("No rows deleted.")
            else if (rowsAffected == 1) Ok("Meal successfully deleted.")
            else InternalServerError("More than 1 row deleted.")
          }
      }
      .getOrElse {
        Future.successful(Unauthorized(UNAUTH_MSG))
      }
  }

  /**
   * Updates meal slot in database with new recipe.
   *
   * @param mealSlotId  Id of meal slot to be updated.
   * @param newRecipeId Id of new recipe.
   * @return Response on whether meal was successfully updated.
   */
  def updateMealSlot(mealSlotId: Int, newRecipeId: Int): Action[AnyContent] = Action.async { request =>
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        database.updateMealSlot(userId, mealSlotId, newRecipeId).map { rowsAffected =>
          if (rowsAffected == 0) InternalServerError("No rows were updated.")
          else if (rowsAffected == 1) Ok("Meal successfully updated.")
          else InternalServerError("More than 1 row updated.")
        }
      }
      .getOrElse {
        Future.successful(Unauthorized(UNAUTH_MSG))
      }
  }
}
