import play.api.db.Database
import play.api.libs.json.{ Json, OFormat }

import java.sql.Connection
import java.util.Date
import scala.concurrent.Future

package object models {
  val SESSION_KEY = "USERID"
  val UNAUTH_MSG = "You are not logged in."

  final def DbConnection[T](
    block: Connection => T
  )(implicit
    executor: DatabaseExecutionContext,
    db: Database
  ): Future[T] =
    Future(db.withConnection(block))

  case class Recipe(
    id: Int,
    name: String,
    mealType: String,
    desc: String,
    time: Int,
    difficulty: String,
    ingredients: String,
    calories: Int,
    fats: Float,
    proteins: Float,
    carbohydrates: Float,
    preferences: Preferences,
    imageUrl: String
  )

  object Recipe {
    implicit val formats: OFormat[Recipe] = Json.format[Recipe]
  }

  case class Preferences(
    isVegan: Boolean = false,
    isVegetarian: Boolean = false,
    isKeto: Boolean = false,
    isLactose: Boolean = false,
    isHalal: Boolean = false,
    isKosher: Boolean = false,
    isDairyFree: Boolean = false,
    isLowCarbs: Boolean = false,
    isGlutenFree: Boolean = false,
    isPeanuts: Boolean = false,
    isEggs: Boolean = false,
    isFish: Boolean = false,
    isTreeNuts: Boolean = false,
    isSoy: Boolean = false,
    targetCalories: Option[Int] = None
  )

  object Preferences {
    implicit val formats: OFormat[Preferences] = Json.format[Preferences]
  }

  case class MovedMealSlot(mealSlotId: Int, date: Date, mealNum: Int)

  object MovedMealSlot {
    implicit val formats: OFormat[MovedMealSlot] = Json.format[MovedMealSlot]
  }

  case class MealSlot(mealSlotId: Int, recipe: Recipe)

  object MealSlot {
    implicit val formats: OFormat[MealSlot] = Json.format[MealSlot]
  }

  case class MetricsWithLabel(
    date: String,
    label: String,
    metrics: Seq[Metrics]
  )
  object MetricsWithLabel {
    implicit val formats: OFormat[MetricsWithLabel] = Json.format[MetricsWithLabel]
  }

  case class Metrics(
    date: String,
    totalCalories: Int,
    totalFats: Double,
    totalProteins: Double,
    totalCarbs: Double
  )

  object Metrics {
    implicit val formats: OFormat[Metrics] = Json.format[Metrics]
  }

  case class ReceivedMealSlot(date: Date, recipeId: Int)

  object ReceivedMealSlot {
    implicit val formats: OFormat[ReceivedMealSlot] = Json.format[ReceivedMealSlot]
  }

  case class FetchedMealSlot(mealSlotId: Int, date: Date, mealNum: Int, recipe: Recipe)

  object FetchedMealSlot {
    implicit val formats: OFormat[FetchedMealSlot] = Json.format[FetchedMealSlot]
  }

  case class UpdateMealSlot(date: Date, mealNum: Int, oldRecipeId: Int, newRecipeId: Int)

  object UpdateMealSlot {
    implicit val formats: OFormat[UpdateMealSlot] = Json.format[UpdateMealSlot]
  }

  case class LoginData(username: String, password: String)

  object LoginData {
    implicit val formats: OFormat[LoginData] = Json.format[LoginData]
  }

  case class RegisterData(email: String, username: String, password: String, preferences: Preferences)

  object RegisterData {
    implicit val formats: OFormat[RegisterData] = Json.format[RegisterData]
  }

  case class Ingredients(
    ingredientId: Int,
    name: String,
    calories: Int,
    fats: Double,
    proteins: Double,
    carbs: Double
  )

  object Ingredients {
    implicit val formats: OFormat[Ingredients] = Json.format[Ingredients]
  }
}
