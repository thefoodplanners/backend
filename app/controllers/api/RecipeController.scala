package controllers.api

import models._
import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.mvc._

import java.io.File
import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * This controller handles HTTP requests dealing
 * with recipes.
 */
@Singleton
class RecipeController @Inject()(cc: ControllerComponents, database: RecipeDao, imageDao: ImageDao)
  (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def getAllRecipes: Action[AnyContent] = Action.async {
    val recipesJsonString = database.fetchAllRecipes.map { recipes =>
      Json.stringify(Json.obj("recipes" -> recipes))
    }
    recipesJsonString.map(Ok(_))
  }

  def getRecipeFromId(recipeId: Int): Action[AnyContent] = Action.async {
    val recipeJsonString = database.fetchRecipe(recipeId).map { recipe =>
      Json.stringify(Json.obj("recipe" -> recipe))
    }
    recipeJsonString.map(Ok(_))
  }

  def getRecommendations: Action[AnyContent] = Action.async { request =>
    request.session
      .get(SESSION_KEY)
      .map { userId =>
        // Fetch recipes from database and split it into groups of 3.
        database.fetchRecommendations(userId.toInt).flatMap { recipes =>
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
        val mealSlotsJson = database.fetchAllMealSlots(userId.toInt, weekDateString).flatMap { mealSlots =>
          val mealSlotsWithImg = mealSlotImageRefToString(mealSlots.map(_.recipe)).map { recipes =>
            mealSlots.zip(recipes).map { case (mealSlot, recipeWithImg) =>
              mealSlot.copy(recipe = recipeWithImg)
            }
          }

          mealSlotsWithImg.map { mealSlots =>
            val mealSlotsArray = mealSlotToArray(mealSlots)
            Json.toJson(mealSlotsArray)
          }
        }
        mealSlotsJson.map(Ok(_))
      }
      .getOrElse {
        Future.successful(Unauthorized("Sorry buddy, not allowed in"))
      }
  }

  private def mealSlotImageRefToString(recipes: Seq[Recipe]): Future[Seq[Recipe]] = {
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
  private def mealSlotToArray(mealSlots: Seq[FetchedMealSlot]): Seq[Seq[Recipe]] = {
    val daySlots = mealSlots
      .groupBy { fetchedMealSlot =>
        val dayOfWeekNum = LocalDate.fromDateFields(fetchedMealSlot.date).getDayOfWeek
        dayOfWeekNum - 1
      }

    Seq.tabulate(7) { index =>
      daySlots
        .get(index)
        .map(_.map(_.recipe))
        .getOrElse(Seq.empty)
    }
  }


  def testImage: Action[AnyContent] = Action {
    Ok.sendFile(new File("./public/images/cat.jpg"))
  }

}
