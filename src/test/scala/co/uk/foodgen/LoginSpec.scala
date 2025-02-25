package co.uk.foodgen

import cats.data.Kleisli
import cats.effect.IO
import co.uk.foodgen.endpoints.*
import co.uk.foodgen.helpers.LoginHelper
import co.uk.foodgen.payload.{LoginRequest, RegisterRequest}
import doobie.util.transactor.Transactor
import org.http4s.Method.*
import org.http4s.Status.*
import org.http4s.{Method, Request, Response, Uri}
import org.typelevel.ci.CIString

object LoginSpec extends IOSuite:
  private val app = (tx: Transactor[IO]) => Server.mainHttpRoutes(tx).orNotFound
  private def loginEndpointsTest = testWithDb(app)(name => expects => test(name)(expects))

  loginEndpointsTest("Fail registering user if user is already registered") { implicit router =>
    val resource = RegisterRequest(
      email = "test@example.com",
      username = "username1",
      password = "password1",
      targetCalories = None,
      dietaryRequirements = List.empty
    )
    val request = Request[IO](POST, Uri.unsafeFromString(registerUrl))
      .withEntity(resource)

    for
      _ <- LoginHelper.createUser(
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

  loginEndpointsTest("Successfully register the user") { implicit router =>
    val resource = RegisterRequest(
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

  loginEndpointsTest("Fail login if the user provides the wrong username") { implicit router =>
    val resource = LoginRequest(username = "wrongusername", password = "password1")
    val request = Request[IO](POST, Uri.unsafeFromString(loginUrl))
      .withEntity(resource)

    for
      _ <- LoginHelper.createUser(
        email = "test@example.com",
        username = "username1",
        password = "password1",
        targetCalories = None,
        dietaryRequirements = List.empty
      )
      response <- router(request)
      check = expect.eql(Unauthorized, response.status)
    yield check
  }

  loginEndpointsTest("Fail login if the user provides the wrong password") { implicit router =>
    val resource = LoginRequest(username = "username1", password = "wrongpassword")
    val request = Request[IO](POST, Uri.unsafeFromString(loginUrl))
      .withEntity(resource)

    for
      _ <- LoginHelper.createUser(
        email = "test@example.com",
        username = "username1",
        password = "password1",
        targetCalories = None,
        dietaryRequirements = List.empty
      )
      response <- router(request)
      check = expect.eql(Unauthorized, response.status)
    yield check
  }

  loginEndpointsTest("Successfully login the user") { implicit router =>
    val resource = LoginRequest(username = "username1", password = "password1")
    val request = Request[IO](POST, Uri.unsafeFromString(loginUrl)).withEntity(resource)

    for
      _ <- LoginHelper.createUser(
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

  loginEndpointsTest("Successfully logout the user and invalidate the session cookie") { implicit router =>
    for
      loginToken <- LoginHelper.loginUser()
      resource = LoginRequest(username = "username1", password = "password1")
      request = Request[IO](POST, Uri.unsafeFromString(logoutUrl)).addCookie(loginToken).withEntity(resource)
      response <- router(request)
      invalidRequest <- router(request)
      check = expect.eql(NoContent, response.status) and expect.eql(Unauthorized, invalidRequest.status)
    yield check
  }

end LoginSpec
