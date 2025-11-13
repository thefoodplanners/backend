package co.uk.foodgen.api.endpoints

import co.uk.foodgen.payload.responses.{UserDietaryRequirementsResponse, UserTargetCaloriesResponse}
import zio.*
import zio.http.*
import zio.http.codec.{Doc, HttpContentCodec, PathCodec}
import zio.http.endpoint.*
import zio.schema.Schema

object UserEndpoints extends APIEndpoint:
  val getUserTargetCalories =
    Endpoint(RoutePattern.GET / "users" / "target-calories")
      .auth(AuthType.Bearer)
      .out[UserTargetCaloriesResponse](Status.Ok, Doc.p(""))
      .outError[Unit](Status.Unauthorized, Doc.p(""))

  val getUserDietaryRequirements =
    Endpoint(RoutePattern.GET / "users" / "dietary-requirements")
      .auth(AuthType.Bearer)
      .out[UserDietaryRequirementsResponse](Status.Ok, Doc.p(""))
      .outError[Unit](Status.Unauthorized, Doc.p(""))

  override val endpoints: List[Endpoint[_, _, _, _, _]] = List(getUserTargetCalories, getUserDietaryRequirements)
end UserEndpoints
