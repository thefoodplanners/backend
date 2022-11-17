import play.api.libs.json.{Json, OFormat}

package object models {
  val SESSION_USERNAME_KEY = "username"

  case class Recipe(
    name: String, foodType: String, desc: String, time: String,
    difficulty: String, ingredients: String, instructions: String,
    calories: String, fats: String
  )
  object Recipe {
    implicit val formats: OFormat[Recipe] = Json.format[Recipe]
  }
}
