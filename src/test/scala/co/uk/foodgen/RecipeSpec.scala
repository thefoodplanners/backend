package co.uk.foodgen

import cats.data.Kleisli
import cats.effect.IO
import cats.implicits.toSemigroupKOps
import co.uk.foodgen.endpoints.withAuthentication
import co.uk.foodgen.helpers.RecipesHelper.addRecipe
import co.uk.foodgen.models.DietaryRequirement.*
import co.uk.foodgen.payload.RecipesResponse
import co.uk.foodgen.service.RecipeService
import doobie.util.transactor.Transactor
import org.http4s.Method.GET
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.{HttpApp, Request, Response, Uri}
import cats.syntax.eq.catsSyntaxEq

object RecipeSpec extends IOSuite:
  private val app = (tx: Transactor[IO]) => Server.mainHttpRoutes(tx).orNotFound -> new RecipeService(using tx)
  private def recipeRoutesTest = testWithDb(app)(name => expects => test(name)(expects))

  recipeRoutesTest("Searching for recipes should only return recipes matching the query") {
    case (router @ given HttpApp[IO], service @ given RecipeService) =>
      for
        _ <- addRecipe("recipe12")
        _ <- addRecipe("recipe2")

        loginToken <- UsersHelper.loginUser()
        request = Request[IO](
          GET,
          Uri.unsafeFromString(s"${recipeSearchUrl.asString}?query=recipe1&limit=5&offset=0")
        )
          .addCookie(loginToken)
        response <- router(request)
        actual <- response.as[RecipesResponse]
        expected = List("recipe12")
        check = expect.eql(actual.recipes.map(_.name), expected)
      yield check
  }

  recipeRoutesTest(
    "Searching for recipes should only return recipes that have the dietary requirements"
  ) { case (router @ given HttpApp[IO], service @ given RecipeService) =>
    for
      _ <- addRecipe(
        "recipe1",
        diets = List(DietaryRequirement.Halal, DietaryRequirement.Vegetarian)
      )
      _ <- addRecipe("recipe2", diets = List(DietaryRequirement.Halal))

      loginToken <- UsersHelper.loginUser()
      request = Request[IO](
        GET,
        Uri.unsafeFromString(
          s"${recipeSearchUrl.asString}?diets=halal&diets=vegetarian&limit=3&offset=0"
        )
      )
        .addCookie(loginToken)
      response <- router(request)
      actual <- response.as[RecipesResponse]
      expected = List("recipe1")
      check = expect.eql(actual.recipes.map(_.name), expected)
    yield check
  }

  recipeRoutesTest(
    "Searching for recipes should only return recipes that are below the calorie limit"
  ) { case (router @ given HttpApp[IO], service @ given RecipeService) =>
    for
      _ <- addRecipe("recipe1", calories = 11)
      _ <- addRecipe("recipe2", calories = 10)
      _ <- addRecipe("recipe3", calories = 9)

      loginToken <- UsersHelper.loginUser()
      request = Request[IO](
        GET,
        Uri.unsafeFromString(s"${recipeSearchUrl.asString}?max-calories=10&limit=10&offset=0")
      )
        .addCookie(loginToken)
      response <- router(request)
      actual <- response.as[RecipesResponse]
      expected = List("recipe2", "recipe3")
      check = expect.eql(actual.recipes.map(_.name), expected)
    yield check
  }

  recipeRoutesTest(
    "Searching for recipes should only return recipes that are below the calorie limit"
  ) { case (router @ given HttpApp[IO], service @ given RecipeService) =>
    for
      _ <- addRecipe("recipe1", carbohydrates = 10.1f)
      _ <- addRecipe("recipe2", carbohydrates = 10.0f)
      _ <- addRecipe("recipe3", carbohydrates = 9.9f)

      loginToken <- UsersHelper.loginUser()
      request = Request[IO](
        GET,
        Uri.unsafeFromString(s"${recipeSearchUrl.asString}?max-carbohydrates=10&limit=10&offset=0")
      )
        .addCookie(loginToken)
      response <- router(request)
      actual <- response.as[RecipesResponse]
      expected = List("recipe2", "recipe3")
      check = expect.eql(expected, actual.recipes.map(_.name))
    yield check
  }

  recipeRoutesTest(
    "Searching for recipes should only return recipes that are below the calorie limit"
  ) { case (router @ given HttpApp[IO], service @ given RecipeService) =>
    for
      _ <- addRecipe("recipe1", proteins = 10.1f)
      _ <- addRecipe("recipe2", proteins = 10.0f)
      _ <- addRecipe("recipe3", proteins = 9.9f)

      loginToken <- UsersHelper.loginUser()
      request = Request[IO](
        GET,
        Uri.unsafeFromString(s"${recipeSearchUrl.asString}?max-proteins=10&limit=10&offset=0")
      )
        .addCookie(loginToken)
      response <- router(request)
      actual <- response.as[RecipesResponse]
      expected = List("recipe2", "recipe3")
      check = expect.eql(actual.recipes.map(_.name), expected)
    yield check
  }

  recipeRoutesTest(
    "Searching for recipes should only return recipes that are below the calorie limit"
  ) { case (router @ given HttpApp[IO], service @ given RecipeService) =>
    for
      _ <- addRecipe("recipe1", fats = 10.1f)
      _ <- addRecipe("recipe2", fats = 10.0f)
      _ <- addRecipe("recipe3", fats = 9.9f)

      loginToken <- UsersHelper.loginUser()
      request = Request[IO](
        GET,
        Uri.unsafeFromString(s"${recipeSearchUrl.asString}?max-fats=10&limit=10&offset=0")
      )
        .addCookie(loginToken)
      response <- router(request)
      actual <- response.as[RecipesResponse]
      expected = List("recipe2", "recipe3")
      check = expect.eql(actual.recipes.map(_.name), expected)
    yield check
  }

  recipeRoutesTest(
    "Searching for recipes should only return a set number of recipes"
  ) { case (router @ given HttpApp[IO], service @ given RecipeService) =>
    for
      _ <- addRecipe("recipe1")
      _ <- addRecipe("recipe2")
      _ <- addRecipe("recipe3")
      _ <- addRecipe("recipe4")

      loginToken <- UsersHelper.loginUser()
      request =
        Request[IO](GET, Uri.unsafeFromString(s"${recipeSearchUrl.asString}?limit=3&offset=0"))
          .addCookie(loginToken)
      response <- router(request)
      actual <- response.as[RecipesResponse]
      expected = List("recipe1", "recipe2", "recipe3")
      check = expect.eql(actual.recipes.map(_.name), expected)
    yield check
  }

  recipeRoutesTest(
    "Searching for recipes should return recipes that are after the limit with offset"
  ) { case (router @ given HttpApp[IO], service @ given RecipeService) =>
    for
      _ <- addRecipe("recipe1")
      recipes <- service.search(limit = 10, offset = 0)
      _ <- addRecipe("recipe2")
      _ <- addRecipe("recipe3")
      _ <- addRecipe("recipe4")
      _ <- addRecipe("recipe5")
      _ <- addRecipe("recipe6")

      loginToken <- UsersHelper.loginUser()
      request =
        Request[IO](GET, Uri.unsafeFromString(s"${recipeSearchUrl.asString}?limit=3&offset=3"))
          .addCookie(loginToken)
      response <- router(request)
      actual <- response.as[RecipesResponse]
      expected = List("recipe4", "recipe5", "recipe6")
      check = expect.eql(actual.recipes.map(_.name), expected)
    yield check
  }

  recipeRoutesTest(
    "Recipe recommendations should return recipes that meet users dietary requirements"
  ) { case (router @ given HttpApp[IO], service @ given RecipeService) =>
    for
      _ <- addRecipe(
        "recipe1",
        diets = List(DietaryRequirement.Vegan, DietaryRequirement.Vegetarian)
      )
      _ <- addRecipe("recipe2", diets = List(DietaryRequirement.Vegan))
      _ <- addRecipe("recipe3")

      loginToken <- UsersHelper.loginUser(dietaryRequirements =
        List(DietaryRequirement.Vegan, DietaryRequirement.Vegetarian)
      )
      request = Request[IO](
        GET,
        Uri.unsafeFromString(recipeRecommendationsUrl.asString)
      )
        .addCookie(loginToken)
      response <- router(request)
      actual <- response.as[RecipesResponse]
      expected = List("recipe1")
      check = expect.eql(actual.recipes.map(_.name), expected)
    yield check
  }

end RecipeSpec
