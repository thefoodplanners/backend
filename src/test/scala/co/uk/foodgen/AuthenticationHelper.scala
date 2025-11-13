package co.uk.foodgen

import co.uk.foodgen.api.toResponseError
import co.uk.foodgen.domain.DietaryRequirement
import co.uk.foodgen.httpApp
import co.uk.foodgen.payload.requests.{LoginRequest, RegisterRequest}
import zio.http.*
import zio.http.Header.Authorization
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec
import zio.{Scope, ZIO}

import javax.sql.DataSource

object AuthenticationHelper:
  def registerUser(
    email: String = "test@email.com",
    username: String = "username1",
    password: String = "password1",
    targetCalories: Option[Int] = None,
    dietaryRequirements: List[DietaryRequirement] = Nil
  ) =
    val requestBody = RegisterRequest(email, username, password, targetCalories, dietaryRequirements)
    val request = Request.post("/register", Body.from(requestBody))
    httpApp(request)

  def loginUser(
    email: String = "test@email.com",
    username: String = "username1",
    password: String = "password1",
    targetCalories: Option[Int] = None,
    dietaryRequirements: List[DietaryRequirement] = Nil
  ): ZIO[Scope & DataSource, Response, Header.Authorization.Bearer] =
    for
      _ <- registerUser(email, username, password, targetCalories, dietaryRequirements)
      requestBody = LoginRequest(username, password)
      request = Request.post("/login", Body.from(requestBody))
      response <- httpApp(request)
      maybeJwtClaim = response.header(Header.Authorization.Bearer)
      jwtClaim <- ZIO.attempt(maybeJwtClaim.get).toResponseError
    yield jwtClaim

end AuthenticationHelper
