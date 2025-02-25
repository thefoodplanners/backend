package co.uk.foodgen

import co.uk.foodgen.models.User
import sttp.tapir.*
import tsec.authentication.AuthenticatedCookie
import tsec.mac.jca.HMACSHA256

package object endpoints:
  type AuthInfo = (User.Id, AuthenticatedCookie[HMACSHA256, User.Id])

  val registerUrl = "register"
  val loginUrl = "login"
  val logoutUrl = "logout"
  val targetCaloriesUrl = "users" / "target-calories"
  val dietaryRequirementsUrl = "users" / "dietary-requirements"
  val recipeSearchUrl = "recipes" / "search"
  val recipeRecommendationsUrl = "recipes" / "recommendations"
  val calendarMealsUrl = "calendar" / "meals"
  val progressChartUrl = "progress-chart"

  extension [A](inp: EndpointInput[A]) def asString: String = inp.show.filterNot(_.isWhitespace)

end endpoints
