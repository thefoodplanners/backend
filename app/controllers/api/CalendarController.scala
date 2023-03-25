package controllers.api

import models._
import org.joda.time.LocalDate
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._

import java.io.File
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
  imageDao: ImageDao,
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
  def getRecommendations: Action[AnyContent] = Action.async { request =>
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        // Fetch recipes from database and split it into groups of 3.
        database.fetchRecommendations(userId).flatMap { recipes =>
          mealSlotImageRefToString(recipes).map { recipesWithImg =>
            val splitRecipes = recipesWithImg.grouped(3).toSeq
            Ok(Json.toJson(splitRecipes))
          }
        }
      }
      .getOrElse {
        Future.successful(Unauthorized("Sorry buddy, not allowed in"))
      }
  }

  def generateWeeklyMealPlan: Action[AnyContent] = Action.async { request =>
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        databaseUser.fetchMaxCalories(userId).flatMap { maxCalories =>
          database.fetchRecommendations(userId).map { recipes =>
            val recipesByMealType = recipes.groupBy(_.mealType)
            val weeklyMeal = Seq.tabulate(7) { _ =>
              val rand = Random
              val pick = rand.nextInt(3)
              pick match {
                case 0 => dayMealPlan(recipesByMealType, maxCalories, "Breakfast", rand)
                case 1 => dayMealPlan(recipesByMealType, maxCalories, "Lunch", rand)
                case 2 => dayMealPlan(recipesByMealType, maxCalories, "Dinner", rand)
              }
            }

            Ok(Json.toJson(weeklyMeal))
          }
        }
      }
      .getOrElse {
        Future.successful(Unauthorized("Sorry buddy, not allowed in"))
      }
  }

  private def dayMealPlan(
    recipesByMealType: Map[String, Seq[Recipe]],
    maxCalories: Int,
    lastMealType: String,
    rand: Random
  ): Seq[Recipe] = {
    val mealTypes = Seq("Breakfast", "Lunch", "Dinner")

    val randomRecipes = mealTypes
      .filterNot(_ == lastMealType)
      .map(mealType => getRandomElem(recipesByMealType(mealType), rand))

    val caloriesLeft = maxCalories - randomRecipes.map(_.calories).sum
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

  private def getRandomElem[A](seq: Seq[A], random: Random): A =
    seq(random.nextInt(seq.length))

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
        database.fetchAllMealSlots(userId, weekDateString).flatMap { mealSlots =>
          val mealSlotsWithImg = mealSlotImageRefToString(mealSlots.map(_.recipe)).map { recipes =>
            mealSlots.zip(recipes).map { case (mealSlot, recipeWithImg) =>
              mealSlot.copy(recipe = recipeWithImg)
            }
          }

          mealSlotsWithImg.map { mealSlots =>
            val mealSlotsArray = mealSlotToArray(mealSlots)
            Ok(Json.toJson(mealSlotsArray))
          }
        }
      }
      .getOrElse {
        Future.successful(Unauthorized("Sorry buddy, not allowed in"))
      }
  }

  def mealSlotImageRefToString(recipes: Seq[Recipe]): Future[Seq[Recipe]] = {
    val imageRefs: Seq[String] = recipes.map(_.imageRef)

    imageDao.fetchImages(imageRefs).map { _ =>
      val nameWithImageString = imageDao.imagesToString

      recipes.map { recipe =>
        val fileName = recipe.imageRef.split("/").last
        nameWithImageString
          .get(fileName)
          .map(imageString => recipe.copy(imageRef = imageString))
          .getOrElse(recipe)
      }
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

    Seq.tabulate(7) { index =>
      daySlots
        .get(index)
        .map(_.map(mealSlot => MealSlot(mealSlot.mealSlotId, mealSlot.recipe)))
        .getOrElse(Seq.empty)
    }
  }

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
        Future.successful(Unauthorized("Sorry buddy, not allowed in."))
      }
  }

  def deleteMealSlot(mealSlotId: Int): Action[AnyContent] = Action.async { request =>
    // Fetch user id from session cookie
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        // Convert request body json to case class
        database
          .deleteMealSlot(userId, mealSlotId)
          .map { rowsAffected =>
            if (rowsAffected == 0) InternalServerError("No rows deleted.")
            else if (rowsAffected == 1) Ok("Meal successfully deleted.")
            else InternalServerError("More than 1 row deleted.")
          }
      }
      .getOrElse {
        Future.successful(Unauthorized("Sorry buddy, not allowed in."))
      }
  }

  def updateMealSlot(mealSlotId: Int, newRecipeId: Int): Action[JsValue] = Action.async(parse.json) { request =>
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
        Future.successful(Unauthorized("Sorry buddy, not allowed in."))
      }
  }

  def testImage: Action[AnyContent] = Action {
    Ok.sendFile(new File("./public/images/cat.jpg"))
  }

}
