package co.uk.foodgen.helpers

import cats.effect.IO
import cats.syntax.eq.catsSyntaxEq
import co.uk.foodgen.endpoints.{loginUrl, registerUrl}
import co.uk.foodgen.models.DietaryRequirement
import co.uk.foodgen.payload.{RegisterRequest, LoginRequest}
import org.http4s.Method.POST
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.{HttpApp, Request, RequestCookie, Uri}

object LoginHelper:
  def loginUser(
    email: String = "test@example.com",
    username: String = "username",
    password: String = "password",
    targetCalories: Option[Int] = None,
    dietaryRequirements: List[DietaryRequirement] = Nil
  )(using router: HttpApp[IO]): IO[RequestCookie] =
    for
      _ <- createUser(email, username, password, targetCalories, dietaryRequirements)
      resource = LoginRequest(username, password)
      request = Request[IO](POST, Uri.unsafeFromString(loginUrl)).withEntity(resource)
      response <- router(request).map(_.cookies.find(_.name === "SESSION_KEY").get)
      loginToken = RequestCookie(response.name, response.content)
    yield loginToken

  def createUser(
    email: String,
    username: String,
    password: String,
    targetCalories: Option[Int] = None,
    dietaryRequirements: List[DietaryRequirement] = Nil
  )(using router: HttpApp[IO]): IO[Unit] =
    val resource = RegisterRequest(email, username, password, targetCalories, dietaryRequirements)
    val request = Request[IO](POST, Uri.unsafeFromString(registerUrl)).withEntity(resource)
    router(request).void

end LoginHelper
