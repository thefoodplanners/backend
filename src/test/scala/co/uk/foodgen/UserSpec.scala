package co.uk.foodgen

import cats.data.Kleisli
import cats.effect.IO
import co.uk.foodgen.endpoints.*
import co.uk.foodgen.helpers.UsersHelper
import co.uk.foodgen.models.DietaryRequirement
import co.uk.foodgen.payload.{DietaryRequirementsPayload, TargetCaloriesResponse}
import doobie.util.transactor.Transactor
import org.http4s.Method.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*
import org.http4s.{Method, Request, Response, Uri}

object UserSpec extends IOSuite:
  private val app = (tx: Transactor[IO]) => Server.mainHttpRoutes(tx).orNotFound
  private def userRoutesTest = testWithDb(app)(name => expects => test(name)(expects))

  userRoutesTest("Users should not be able to access authorised endpoints if not logged in") { implicit router =>
    for
      loginToken <- UsersHelper.loginUser(targetCalories = Some(100))
      request = Request[IO](GET, Uri.unsafeFromString(targetCaloriesUrl.asString))
        .addCookie(loginToken)
      response <- router(request)
      _ <- IO.println(response.status)
      targetCalories <- response.as[TargetCaloriesResponse]
      expected = TargetCaloriesResponse(Some(100))
      check = expect.eql(Ok, response.status) and expect.same(targetCalories, expected)
    yield check
  }

  userRoutesTest(
    "GET / users / dietary-requirements should return the correct dietary requirements"
  ) { implicit router =>
    for
      loginToken <- UsersHelper.loginUser(dietaryRequirements = List(DietaryRequirement.Vegan))
      request = Request[IO](GET, Uri.unsafeFromString(dietaryRequirementsUrl.asString))
        .addCookie(loginToken)
      response <- router(request)
      result <- response.as[DietaryRequirementsPayload]
      expected = DietaryRequirementsPayload(List(DietaryRequirement.Vegan))
      check = expect.eql(response.status, Ok) and expect.same(result, expected)
    yield check
  }

  userRoutesTest(
    "PUT / users / dietary-requirements should update to the correct dietary requirements"
  ) { implicit router =>
    for
      loginToken <- UsersHelper.loginUser(dietaryRequirements =
        List(DietaryRequirement.Vegan, DietaryRequirement.Vegetarian)
      )
      expected =
        DietaryRequirementsPayload(List(DietaryRequirement.Vegetarian, DietaryRequirement.Halal))
      putRequest = Request[IO](PUT, Uri.unsafeFromString(dietaryRequirementsUrl.asString))
        .addCookie(loginToken)
        .withEntity(expected)
      getRequest = Request[IO](GET, Uri.unsafeFromString(dietaryRequirementsUrl.asString))
        .addCookie(loginToken)
      putResponse <- router(putRequest)
      getResponse <- router(getRequest)
      result <- getResponse.as[DietaryRequirementsPayload]
      check = expect.eql(NoContent, putResponse.status) and
        expect.eql(Ok, getResponse.status) and
        expect.same(result, expected)
    yield check
  }

end UserSpec
