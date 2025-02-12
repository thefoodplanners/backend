package co.uk.foodgen.endpoints

import cats.effect.IO
import co.uk.foodgen.models.User
import co.uk.foodgen.payload.{DietaryRequirementsPayload, TargetCaloriesResponse}
import co.uk.foodgen.service.UserService
import doobie.util.transactor.Transactor
import org.http4s.ContextRoutes
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.*

class UserEndpoints(transactor: Transactor[IO]):

  private lazy val userService = new UserService(using transactor)

  private val getTargetCalories = endpoint.get
    .in(targetCaloriesUrl)
    .contextIn[User.Id]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(jsonBody[TargetCaloriesResponse])
    .serverLogicSuccess[IO](userId =>
      userService
        .getTargetCalories(userId)
        .map(TargetCaloriesResponse(_))
    )

  private val getDietaryRequirements = endpoint.get
    .in(dietaryRequirementsUrl)
    .contextIn[User.Id]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(jsonBody[DietaryRequirementsPayload])
    .serverLogicSuccess[IO](userId =>
      userService
        .getDietaryRequirements(userId)
        .map(DietaryRequirementsPayload(_))
    )

  private val updateDietaryRequirements = endpoint.put
    .in(dietaryRequirementsUrl)
    .contextIn[User.Id]()
    .in(jsonBody[DietaryRequirementsPayload])
    .errorOut(statusCode(StatusCode.Unauthorized))
    .serverLogicSuccess[IO] { (userId, request) =>
      userService.updateDietaryRequirements(request.dietaryRequirements, userId)
    }

  val allEndpoints = List(getTargetCalories, getDietaryRequirements, updateDietaryRequirements)

  val allRoutes: ContextRoutes[User.Id, IO] =
    Http4sServerInterpreter[IO]().toContextRoutes(allEndpoints)

end UserEndpoints
