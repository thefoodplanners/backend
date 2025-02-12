package co.uk.foodgen.endpoints

import cats.effect.IO
import co.uk.foodgen.payload.{CreateUserRequest, LoginRequest}
import co.uk.foodgen.service.LoginService
import co.uk.foodgen.service.errorModels.ServiceError
import doobie.util.transactor.Transactor
import org.http4s.HttpRoutes
import sttp.model.StatusCode
import sttp.model.headers.CookieValueWithMeta
import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.Http4sServerInterpreter

class LoginEndpoints(transactor: Transactor[IO]):

  private lazy val loginService = new LoginService(using transactor)

  private val register = endpoint.post
    .in(registerUrl)
    .in(jsonBody[CreateUserRequest])
    .errorOut(
      oneOf(
        ServiceError.toStatusCode[ServiceError.BadRequest]
      )
    )
    .out(statusCode(StatusCode.Created))
    .serverLogic[IO](request =>
      loginService
        .register(
          email = request.email,
          username = request.username,
          password = request.password,
          targetCalories = request.targetCalories,
          dietaryRequirements = request.dietaryRequirements
        )
        .value
    )

  private val login = endpoint.post
    .in(loginUrl)
    .in(jsonBody[LoginRequest])
    .out(setCookie("SESSION_KEY"))
    .errorOut(statusCode(StatusCode.Unauthorized))
    .serverLogicOption(request =>
      loginService
        .login(request.username, request.password)
        .map(_.map(CookieValueWithMeta.unsafeApply(_)))
    )

  val allEndpoints = List(register, login)

  val allRoutes: HttpRoutes[IO] = Http4sServerInterpreter[IO]().toRoutes(allEndpoints)

end LoginEndpoints
