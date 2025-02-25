package co.uk.foodgen.endpoints

import cats.data.Ior
import cats.effect.IO
import co.uk.foodgen.payload.{LoginRequest, RegisterRequest}
import co.uk.foodgen.service.LoginService
import co.uk.foodgen.service.errorModels.ServiceError
import doobie.util.transactor.Transactor
import org.http4s.{ContextRoutes, HttpRoutes}
import sttp.model.StatusCode
import sttp.model.headers.CookieValueWithMeta
import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import sttp.tapir.server.http4s.*
import tsec.authentication.AuthenticatedCookie
import tsec.mac.jca.HMACSHA256

class LoginEndpoints(transactor: Transactor[IO]) extends HttpEndpoint:

  private lazy val loginService = new LoginService(using transactor)

  private val register = endpoint.post
    .in(registerUrl)
    .in(jsonBody[RegisterRequest])
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

  private val logout = endpoint.post
    .in(logoutUrl)
    .contextIn[AuthInfo]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(statusCode(StatusCode.NoContent))
    .serverLogicSuccess[IO]((_, auth) => loginService.logout(auth))

  private val normalEndpoints = List(register, login)
  private val authEndpoints = List(logout)
  val endpoints = normalEndpoints ++ authEndpoints

  val routes: Ior[HttpRoutes[IO], ContextRoutes[AuthInfo, IO]] =
    val normalRoutes: HttpRoutes[IO] = Http4sServerInterpreter[IO]().toRoutes(normalEndpoints)
    val contextRoutes: ContextRoutes[AuthInfo, IO] = Http4sServerInterpreter[IO]().toContextRoutes(authEndpoints)
    Ior.both(normalRoutes, contextRoutes)

end LoginEndpoints
