package co.uk.foodgen

import AuthenticationHelper.*
import co.uk.foodgen.{httpApp, setupDbLayer}
import co.uk.foodgen.domain.DietaryRequirement
import zio.http.*
import zio.test.*

import javax.sql.DataSource
import scala.language.strictEquality

object UserSpec extends ZIOSpecDefault:
  def spec =
    suite("Users")(
      suite("Fetching target calories") {
        test("should be successful") {
          for
            authToken <- loginUser(targetCalories = Some(1000))
            request = Request.get("/users/target-calories").addHeader(authToken)
            response <- httpApp(request)
            content <- response.body.asString
          yield assertTrue(
            response.status.code == Status.Ok.code,
            content == "1000"
          )
        }
      },
      suite("Fetching dietary requirements")(
        test("should be successful") {
          for
            authToken <- loginUser(dietaryRequirements = List(DietaryRequirement.Vegan))
            request = Request.get("/users/dietary-requirements").addHeader(authToken)
            response <- httpApp(request)
            content <- response.body.asString
          yield assertTrue(
            response.status.code == Status.Ok.code,
            content == "[\"Vegan\"]"
          )
        },
        test("should be successful when dietary requirements is empty") {
          for
            authToken <- loginUser()
            request = Request.get("/users/dietary-requirements").addHeader(authToken)
            response <- httpApp(request)
            content <- response.body.asString
          yield assertTrue(
            response.status.code == Status.Ok.code,
            content == "[]"
          )
        }
      )
    ).provideSomeLayer(setupDbLayer)
end UserSpec
