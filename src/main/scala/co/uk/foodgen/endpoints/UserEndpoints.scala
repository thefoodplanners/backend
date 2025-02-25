package co.uk.foodgen.endpoints

import cats.data.Ior
import cats.effect.IO
import co.uk.foodgen.payload.{DietaryRequirementsPayload, TargetCaloriesResponse}
import co.uk.foodgen.service.UserService
import doobie.util.transactor.Transactor
import org.http4s.{ContextRoutes, HttpRoutes}
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.*
import tsec.authentication.AuthenticatedCookie
import tsec.mac.jca.HMACSHA256

class UserEndpoints(transactor: Transactor[IO]) extends HttpEndpoint:

  private lazy val userService = new UserService(using transactor)

  private val getTargetCalories = endpoint.get
    .in(targetCaloriesUrl)
    .contextIn[AuthInfo]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(jsonBody[TargetCaloriesResponse])
    .serverLogicSuccess[IO] { (userId, _) =>
      userService
        .getTargetCalories(userId)
        .map(TargetCaloriesResponse(_))
    }

  private val getDietaryRequirements = endpoint.get
    .in(dietaryRequirementsUrl)
    .contextIn[AuthInfo]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(jsonBody[DietaryRequirementsPayload])
    .serverLogicSuccess[IO] { (userId, _) =>
      userService
        .getDietaryRequirements(userId)
        .map(DietaryRequirementsPayload(_))
    }

  private val updateDietaryRequirements = endpoint.put
    .in(dietaryRequirementsUrl)
    .contextIn[AuthInfo]()
    .in(jsonBody[DietaryRequirementsPayload])
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(statusCode(StatusCode.NoContent))
    .serverLogicSuccess[IO] { (userId, _, request) =>
      userService.updateDietaryRequirements(request.dietaryRequirements, userId)
    }

  val endpoints = List(getTargetCalories, getDietaryRequirements, updateDietaryRequirements)

  val routes: Ior[HttpRoutes[IO], ContextRoutes[AuthInfo, IO]] =
    val authRoutes = Http4sServerInterpreter[IO]().toContextRoutes(endpoints)
    Ior.right(authRoutes)

end UserEndpoints
