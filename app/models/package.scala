import play.api.libs.json.{ Json, OFormat }

package object models {
  val SESSION_USERNAME_KEY = "USERNAME"

  case class Recipe(
    id: Int, name: String, mealType: String, desc: String, time: Int,
    difficulty: String, ingredients: String, instructions: String,
    calories: Int, fats: Int, proteins: Int, carbohydrates: Int,
    preferences: Preferences
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

}
