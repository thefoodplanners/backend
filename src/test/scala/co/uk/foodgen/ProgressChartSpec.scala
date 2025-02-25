package co.uk.foodgen

import cats.data.Kleisli
import cats.effect.IO
import co.uk.foodgen.endpoints.*
import co.uk.foodgen.helpers.{LoginHelper, MealsHelper, RecipesHelper}
import co.uk.foodgen.payload.{FullMetricsResponse, MetricsResponse}
import co.uk.foodgen.service.{MealService, RecipeService}
import doobie.util.transactor.Transactor
import org.http4s.Method.*
import org.http4s.Status.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.{HttpApp, Method, Request, RequestCookie, Response, Uri}

import java.time.LocalDate

object ProgressChartSpec extends IOSuite:
  private val app = (tx: Transactor[IO]) =>
    (
      Server.mainHttpRoutes(tx).orNotFound,
      new RecipeService(using tx),
      new MealService(using tx)
    )
  private def progressChartRoutesTest = testWithDb(app)(name => expects => test(name)(expects))

  private val date = LocalDate.parse("2024-01-01")

  progressChartRoutesTest("Fetching metrics by day should return the correct metrics") {
    case (router @ given HttpApp[IO], _ @ given RecipeService, _ @ given MealService) =>
      for
        recipeId1 <- RecipesHelper.addRecipe(
          name = "recipe1",
          calories = 100,
          carbohydrates = 10f,
          proteins = 10f,
          fats = 10f
        )
        recipeId2 <- RecipesHelper.addRecipe(
          name = "recipe2",
          calories = 200,
          carbohydrates = 20f,
          proteins = 20f,
          fats = 20f
        )
        recipeId3 <- RecipesHelper.addRecipe(
          name = "recipe3",
          calories = 300,
          carbohydrates = 30f,
          proteins = 30f,
          fats = 30f
        )
        loginToken @ given RequestCookie <- LoginHelper.loginUser()
        _ <- MealsHelper.addMeal(date, None, recipeId1)
        _ <- MealsHelper.addMeal(date, None, recipeId2)
        _ <- MealsHelper.addMeal(date, None, recipeId3)
        request =
          Request[IO](GET, Uri.unsafeFromString(s"$progressChartUrl/day/metrics?date=$date"))
            .addCookie(loginToken)
        response <- router(request)
        result <- response.as[FullMetricsResponse]
        expected = FullMetricsResponse(
          label = "Monday",
          metrics = List(
            MetricsResponse("Meal 1", 100, 10f, 10f, 10f),
            MetricsResponse("Meal 2", 200, 20f, 20f, 20f),
            MetricsResponse("Meal 3", 300, 30f, 30f, 30f)
          )
        )
        check = expect.eql(Ok, response.status) and
          expect.same(expected, result)
      yield check
  }

  progressChartRoutesTest("Fetching metrics by week should return the correct metrics") {
    case (router @ given HttpApp[IO], _ @ given RecipeService, _ @ given MealService) =>
      for
        recipeId1 <- RecipesHelper.addRecipe(
          name = "recipe1",
          calories = 100,
          carbohydrates = 10f,
          proteins = 10f,
          fats = 10f
        )
        recipeId2 <- RecipesHelper.addRecipe(
          name = "recipe2",
          calories = 200,
          carbohydrates = 20f,
          proteins = 20f,
          fats = 20f
        )
        recipeId3 <- RecipesHelper.addRecipe(
          name = "recipe3",
          calories = 300,
          carbohydrates = 30f,
          proteins = 30f,
          fats = 30f
        )
        loginToken @ given RequestCookie <- LoginHelper.loginUser()
        _ <- MealsHelper.addMeal(date, None, recipeId1)
        _ <- MealsHelper.addMeal(date.plusDays(1), None, recipeId1)
        _ <- MealsHelper.addMeal(date.plusDays(1), None, recipeId2)
        _ <- MealsHelper.addMeal(date.plusDays(2), None, recipeId1)
        _ <- MealsHelper.addMeal(date.plusDays(2), None, recipeId2)
        _ <- MealsHelper.addMeal(date.plusDays(2), None, recipeId3)
        request =
          Request[IO](GET, Uri.unsafeFromString(s"$progressChartUrl/week/metrics?date=$date"))
            .addCookie(loginToken)
        response <- router(request)
        result <- response.as[FullMetricsResponse]
        expected = FullMetricsResponse(
          label = "Jan 1-7",
          metrics = List(
            MetricsResponse("Mon", 100, 10f, 10f, 10f),
            MetricsResponse("Tue", 300, 30f, 30f, 30f),
            MetricsResponse("Wed", 600, 60f, 60f, 60f)
          )
        )
        check = expect.eql(Ok, response.status) and
          expect.same(expected, result)
      yield check
  }

  progressChartRoutesTest("Fetching metrics by month should return the correct metrics") {
    case (router @ given HttpApp[IO], _ @ given RecipeService, _ @ given MealService) =>
      for
        recipeId1 <- RecipesHelper.addRecipe(
          name = "recipe1",
          calories = 100,
          carbohydrates = 10f,
          proteins = 10f,
          fats = 10f
        )
        recipeId2 <- RecipesHelper.addRecipe(
          name = "recipe2",
          calories = 200,
          carbohydrates = 20f,
          proteins = 20f,
          fats = 20f
        )
        recipeId3 <- RecipesHelper.addRecipe(
          name = "recipe3",
          calories = 300,
          carbohydrates = 30f,
          proteins = 30f,
          fats = 30f
        )
        loginToken @ given RequestCookie <- LoginHelper.loginUser()
        _ <- MealsHelper.addMeal(date, None, recipeId1)
        _ <- MealsHelper.addMeal(date.plusWeeks(1), None, recipeId1)
        _ <- MealsHelper.addMeal(date.plusWeeks(1), None, recipeId2)
        _ <- MealsHelper.addMeal(date.plusWeeks(2), None, recipeId1)
        _ <- MealsHelper.addMeal(date.plusWeeks(2), None, recipeId2)
        _ <- MealsHelper.addMeal(date.plusWeeks(2), None, recipeId3)
        request =
          Request[IO](GET, Uri.unsafeFromString(s"$progressChartUrl/month/metrics?date=$date"))
            .addCookie(loginToken)
        response <- router(request)
        result <- response.as[FullMetricsResponse]
        expected = FullMetricsResponse(
          label = "January",
          metrics = List(
            MetricsResponse("Jan 1-7", 100, 10f, 10f, 10f),
            MetricsResponse("Jan 8-14", 300, 30f, 30f, 30f),
            MetricsResponse("Jan 15-21", 600, 60f, 60f, 60f)
          )
        )
        check = expect.eql(Ok, response.status) and
          expect.same(expected, result)
      yield check
  }

  progressChartRoutesTest("Fetching metrics by month should return the correct metrics") {
    case (router @ given HttpApp[IO], _ @ given RecipeService, _ @ given MealService) =>
      for
        recipeId1 <- RecipesHelper.addRecipe(
          name = "recipe1",
          calories = 100,
          carbohydrates = 10f,
          proteins = 10f,
          fats = 10f
        )
        recipeId2 <- RecipesHelper.addRecipe(
          name = "recipe2",
          calories = 200,
          carbohydrates = 20f,
          proteins = 20f,
          fats = 20f
        )
        recipeId3 <- RecipesHelper.addRecipe(
          name = "recipe3",
          calories = 300,
          carbohydrates = 30f,
          proteins = 30f,
          fats = 30f
        )
        loginToken @ given RequestCookie <- LoginHelper.loginUser()
        _ <- MealsHelper.addMeal(date, None, recipeId1)
        _ <- MealsHelper.addMeal(date.plusMonths(1), None, recipeId1)
        _ <- MealsHelper.addMeal(date.plusMonths(1), None, recipeId2)
        _ <- MealsHelper.addMeal(date.plusMonths(2), None, recipeId1)
        _ <- MealsHelper.addMeal(date.plusMonths(2), None, recipeId2)
        _ <- MealsHelper.addMeal(date.plusMonths(2), None, recipeId3)
        request =
          Request[IO](GET, Uri.unsafeFromString(s"$progressChartUrl/year/metrics?date=$date"))
            .addCookie(loginToken)
        response <- router(request)
        result <- response.as[FullMetricsResponse]
        expected = FullMetricsResponse(
          label = "2024",
          metrics = List(
            MetricsResponse("Jan", 100, 10f, 10f, 10f),
            MetricsResponse("Feb", 300, 30f, 30f, 30f),
            MetricsResponse("Mar", 600, 60f, 60f, 60f)
          )
        )
        check = expect.eql(Ok, response.status) and
          expect.same(expected, result)
      yield check
  }
end ProgressChartSpec
