package co.uk.foodgen

import cats.data.Kleisli
import cats.effect.IO
import co.uk.foodgen.endpoints.*
import co.uk.foodgen.helpers.UsersHelper
import co.uk.foodgen.payload.{CreateUserRequest, LoginRequest}
import doobie.util.transactor.Transactor
import org.http4s.Method.*
import org.http4s.Status.*
import org.http4s.{Method, Request, Response, Uri}
import org.typelevel.ci.CIString

object LoginSpec extends IOSuite:
  private val app = (tx: Transactor[IO]) => Server.mainHttpRoutes(tx).orNotFound
  private def loginEndpointsTest = testWithDb(app)(name => expects => test(name)(expects))

  loginEndpointsTest("Successfully register the user") { implicit router =>
    val resource = CreateUserRequest(
      email = "test@example.com",
      username = "username1",
      password = "password1",
      targetCalories = None,
      dietaryRequirements = List.empty
    )
    val request = Request[IO](POST, Uri.unsafeFromString(registerUrl))
      .withEntity(resource)

    for
      response <- router(request)
      check = expect.eql(response.status, Created)
    yield check
  }

  loginEndpointsTest("Fail registering user if user is already registered") { implicit router =>
    val resource = CreateUserRequest(
      email = "test@example.com",
      username = "username1",
      password = "password1",
      targetCalories = None,
      dietaryRequirements = List.empty
    )
    val request = Request[IO](POST, Uri.unsafeFromString(registerUrl))
      .withEntity(resource)

    for
      _ <- UsersHelper.createUser(
        email = "test@example.com",
        username = "username1",
        password = "password1",
        targetCalories = None,
        dietaryRequirements = List.empty
      )
      response <- router(request)
      errorMessage <- response.as[String]
      check = expect.eql(BadRequest, response.status) and
        expect.eql("Username already exists", errorMessage)
    yield check
  }

  loginEndpointsTest("Successfully login the user") { implicit router =>
    val resource = LoginRequest(username = "username1", password = "password1")
    val request = Request[IO](POST, Uri.unsafeFromString(loginUrl))
      .withEntity(resource)

    for
      _ <- UsersHelper.createUser(
        email = "test@example.com",
        username = "username1",
        password = "password1",
        targetCalories = None,
        dietaryRequirements = List.empty
      )
      response <- router(request)
      sessionKey = response.headers.get(CIString("set-cookie")).get.head.value
      check = expect.eql(Ok, response.status) and
        expect(sessionKey.startsWith("SESSION_KEY="))
    yield check
  }

  loginEndpointsTest("Users should not be able to access authorised endpoints if not logged in") { implicit router =>
    for
      _ <- UsersHelper.createUser(
        email = "test@example.com",
        username = "username",
        password = "password",
        targetCalories = Some(100)
      )
      request = Request[IO](GET, Uri.unsafeFromString(targetCaloriesUrl.asString))
      response <- router(request)
      check = expect.eql(response.status, Unauthorized)
    yield check
  }

end LoginSpec
