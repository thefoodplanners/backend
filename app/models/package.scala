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

  case class LoginData(username: String, password: String)

  object LoginData {
    implicit val formats: OFormat[LoginData] = Json.format[LoginData]
  }

  case class ReceivedMealSlot(date: Date, mealType: String, recipeId: Int, userId: String)

  case class FetchedMealSlot(date: Date, mealNum: Int, recipe: Recipe)

  object FetchedMealSlot {
    implicit val formats: OFormat[FetchedMealSlot] = Json.format[FetchedMealSlot]
  }
}
