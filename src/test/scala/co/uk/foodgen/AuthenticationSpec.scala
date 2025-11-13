package co.uk.foodgen

import AuthenticationHelper.*
import co.uk.foodgen.{httpApp, setupDbLayer}
import co.uk.foodgen.payload.requests.{LoginRequest, RegisterRequest}
import zio.http.*
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec
import zio.test.{ZIOSpecDefault, assertTrue}

import javax.sql.DataSource
import scala.language.strictEquality

object AuthenticationSpec extends ZIOSpecDefault:
  def spec =
    suite("Authentication")(
      suite("Registering users") {
        test("should be successful") {
          val requestBody = RegisterRequest("email", "username", "password", None, Nil)
          val request = Request.post("/register", Body.from(requestBody))
          httpApp(request).map(response => assertTrue(response.status.code == Status.Created.code))
        }
      },
      suite("Logging in users") {
        test("should return JWT Bearer Token") {
          for
            _ <- registerUser()
            requestBody = LoginRequest("username1", "password1")
            request = Request.post("/login", Body.from(requestBody))
            response <- httpApp(request)
          yield assertTrue(
            response.status.code == Status.NoContent.code,
            response.header(Header.Authorization.Bearer).isDefined
          )
        }
      }
    ).provideSomeLayer(setupDbLayer)
end AuthenticationSpec
