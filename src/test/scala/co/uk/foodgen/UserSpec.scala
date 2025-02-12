package co.uk.foodgen

import cats.data.Kleisli
import cats.effect.IO
import cats.implicits.toSemigroupKOps
import co.uk.foodgen.endpoints.*
import co.uk.foodgen.helpers.UsersHelper
import co.uk.foodgen.models.DietaryRequirement
import co.uk.foodgen.payload.{DietaryRequirementsPayload, TargetCaloriesResponse}
import doobie.util.transactor.Transactor
import org.http4s.Method.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.{Method, Request, Response, Uri}

object UserSpec extends IOSuite:
  private val app = (tx: Transactor[IO]) =>
    (new LoginEndpoints(tx).allRoutes <+>
      new UserEndpoints(tx).allRoutes.withAuthentication).orNotFound
  private def userRoutesTest = testWithDb(app)(name => expects => test(name)(expects))

  userRoutesTest("GET / users / target-calories should return the correct dietary requirements") { implicit router =>
    for
      loginToken <- UsersHelper.loginUser(targetCalories = Some(100))
      request = Request[IO](GET, Uri.unsafeFromString(targetCaloriesUrl.asString))
        .addCookie(loginToken)
      response <- router(request)
      _ <- IO.println(response.status)
      targetCalories <- response.as[TargetCaloriesResponse]
      expected = TargetCaloriesResponse(Some(100))
      check = expect.same(targetCalories, expected)
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
      check = expect.same(result, expected)
    yield check
  }

  userRoutesTest(
    "POST / users / dietary-requirements should update to the correct dietary requirements"
  ) { implicit router =>
    for
      loginToken <- UsersHelper.loginUser(dietaryRequirements =
        List(DietaryRequirement.Vegan, DietaryRequirement.Vegetarian)
      )
      expected =
        DietaryRequirementsPayload(List(DietaryRequirement.Vegetarian, DietaryRequirement.Halal))
      postRequest = Request[IO](PUT, Uri.unsafeFromString(dietaryRequirementsUrl.asString))
        .addCookie(loginToken)
        .withEntity(expected)
      getRequest = Request[IO](GET, Uri.unsafeFromString(dietaryRequirementsUrl.asString))
        .addCookie(loginToken)
      _ <- router(postRequest)
      response <- router(getRequest)
      result <- response.as[DietaryRequirementsPayload]
      check = expect.same(result, expected)
    yield check
  }

end UserSpec
