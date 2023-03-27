import play.api.libs.json.{ Json, OFormat }

import java.util.Date

package object models {
  val SESSION_KEY = "USERID"
  val IMAGES_PATH = "./public/images"

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
    imageRef: String
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
    isLowCarbs: Boolean = false
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

  case class ConsumedCalories(date: String, caloriesConsumed: Int)

  object ConsumedCalories {
    implicit val formats: OFormat[ConsumedCalories] = Json.format[ConsumedCalories]
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
}
