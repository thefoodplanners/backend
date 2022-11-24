import play.api.libs.json.{Json, OFormat}

package object models {
  val SESSION_USERNAME_KEY = "username"

  case class Recipe(
    name: String, mealType: String, desc: String, time: String,
    difficulty: String, ingredients: String, instructions: String,
    calories: String, fats: String
  )
  object Recipe {
    implicit val formats: OFormat[Recipe] = Json.format[Recipe]
  }

  case class LoginData(username: String, password: String)
}
