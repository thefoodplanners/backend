import play.api.libs.json.{ Json, OFormat }

import java.util.Date

package object models {
  val SESSION_KEY = "USERID"
  val IMAGES_PATH = "./public/images"

  case class Recipe(
    id: Int, name: String, mealType: String, desc: String, time: Int,
    difficulty: String, ingredients: String, calories: Int, fats: Float,
    proteins: Float, carbohydrates: Float, preferences: Preferences,
    imageRef: String
  )

  object Recipe {
    implicit val formats: OFormat[Recipe] = Json.format[Recipe]
  }

  case class Preferences(
    isVegan: Boolean, isVegetarian: Boolean, isKeto: Boolean,
    isLactose: Boolean, isHalal: Boolean, isKosher: Boolean,
    isDairyFree: Boolean, isLowCarbs: Boolean
  )

  object Preferences {
    implicit val formats: OFormat[Preferences] = Json.format[Preferences]
  }

  case class ReceivedMealSlot(date: Date, mealNum: Int, recipeId: Int)

  object ReceivedMealSlot {
    implicit val formats: OFormat[ReceivedMealSlot] = Json.format[ReceivedMealSlot]
  }

  case class FetchedMealSlot(date: Date, mealNum: Int, recipe: Recipe)

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

  case class RegisterData(email: String, username: String, password: String)

  object RegisterData {
    implicit val formats: OFormat[RegisterData] = Json.format[RegisterData]
  }
}
