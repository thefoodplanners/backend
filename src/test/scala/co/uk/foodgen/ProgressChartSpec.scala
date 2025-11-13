package co.uk.foodgen

import co.uk.foodgen.AuthenticationHelper.loginUser
import co.uk.foodgen.RecipesHelper.addRecipe
import co.uk.foodgen.api.{AuthMiddleware, toResponseError}
import co.uk.foodgen.payload.responses.MetricResponses.*
import co.uk.foodgen.{httpApp, setupDbLayer}
import zio.ZIO
import zio.http.*
import zio.prelude.EqualOps
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec
import zio.test.*

import java.time.LocalDate
import javax.sql.DataSource
import scala.language.strictEquality

object ProgressChartSpec extends ZIOSpecDefault:
  private val date = LocalDate.parse("2024-01-01")

  def spec =
    suite("Progress Chart")(
      test("Fetching metrics by day should return the correct metrics") {
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
          authToken <- loginUser()
          userId <- AuthMiddleware.decodeAuthToken(authToken)
          _ <- MealsHelper.addMeal(date, None, recipeId1, userId)
          _ <- MealsHelper.addMeal(date, None, recipeId2, userId)
          _ <- MealsHelper.addMeal(date, None, recipeId3, userId)
          url <- ZIO.fromEither(URL.decode(s"/progress-chart/day/metrics?date=$date")).toResponseError
          request = Request.get(url).addHeader(authToken)
          response <- httpApp(request)
          actual <- response.bodyAs[FullMetricsResponse]
          expected = FullMetricsResponse(
            label = "Monday",
            metrics = MetricsResponse(
              List("Meal 1", "Meal 2", "Meal 3"),
              List(100, 200, 300),
              List(10f, 20f, 30f),
              List(10f, 20f, 30f),
              List(10f, 20f, 30f)
            )
          )
        yield assertTrue(
          response.status.code === Status.Ok.code,
          actual === expected
        )
      },
      test("Fetching metrics by week should return the correct metrics") {
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
          authToken <- loginUser()
          userId <- AuthMiddleware.decodeAuthToken(authToken)
          _ <- MealsHelper.addMeal(date, None, recipeId1, userId)
          _ <- MealsHelper.addMeal(date.plusDays(1), None, recipeId1, userId)
          _ <- MealsHelper.addMeal(date.plusDays(1), None, recipeId2, userId)
          _ <- MealsHelper.addMeal(date.plusDays(2), None, recipeId1, userId)
          _ <- MealsHelper.addMeal(date.plusDays(2), None, recipeId2, userId)
          _ <- MealsHelper.addMeal(date.plusDays(2), None, recipeId3, userId)
          url <- ZIO.fromEither(URL.decode(s"/progress-chart/week/metrics?date=$date")).toResponseError
          request = Request.get(url).addHeader(authToken)
          response <- httpApp(request)
          actual <- response.bodyAs[FullMetricsResponse]
          expected = FullMetricsResponse(
            label = "Jan 1-7",
            metrics = MetricsResponse(
              List("Mon", "Tue", "Wed"),
              List(100, 300, 600),
              List(10f, 30f, 60f),
              List(10f, 30f, 60f),
              List(10f, 30f, 60f)
            )
          )
        yield assertTrue(
          response.status.code === Status.Ok.code,
          actual === expected
        )
      },
      test("Fetching metrics by month should return the correct metrics") {
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
          authToken <- loginUser()
          userId <- AuthMiddleware.decodeAuthToken(authToken)
          _ <- MealsHelper.addMeal(date, None, recipeId1, userId)
          _ <- MealsHelper.addMeal(date.plusWeeks(1), None, recipeId1, userId)
          _ <- MealsHelper.addMeal(date.plusWeeks(1), None, recipeId2, userId)
          _ <- MealsHelper.addMeal(date.plusWeeks(2), None, recipeId1, userId)
          _ <- MealsHelper.addMeal(date.plusWeeks(2), None, recipeId2, userId)
          _ <- MealsHelper.addMeal(date.plusWeeks(2), None, recipeId3, userId)
          url <- ZIO.fromEither(URL.decode(s"/progress-chart/month/metrics?date=$date")).toResponseError
          request = Request.get(url).addHeader(authToken)
          response <- httpApp(request)
          actual <- response.bodyAs[FullMetricsResponse]
          expected = FullMetricsResponse(
            label = "January",
            metrics = MetricsResponse(
              List("Jan 1-7", "Jan 8-14", "Jan 15-21"),
              List(100, 300, 600),
              List(10f, 30f, 60f),
              List(10f, 30f, 60f),
              List(10f, 30f, 60f)
            )
          )
        yield assertTrue(
          response.status.code === Status.Ok.code,
          actual === expected
        )
      },
      test("Fetching metrics by year should return the correct metrics") {
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
          authToken <- loginUser()
          userId <- AuthMiddleware.decodeAuthToken(authToken)
          _ <- MealsHelper.addMeal(date, None, recipeId1, userId)
          _ <- MealsHelper.addMeal(date.plusMonths(1), None, recipeId1, userId)
          _ <- MealsHelper.addMeal(date.plusMonths(1), None, recipeId2, userId)
          _ <- MealsHelper.addMeal(date.plusMonths(2), None, recipeId1, userId)
          _ <- MealsHelper.addMeal(date.plusMonths(2), None, recipeId2, userId)
          _ <- MealsHelper.addMeal(date.plusMonths(2), None, recipeId3, userId)
          url <- ZIO.fromEither(URL.decode(s"/progress-chart/year/metrics?date=$date")).toResponseError
          request = Request.get(url).addHeader(authToken)
          response <- httpApp(request)
          actual <- response.bodyAs[FullMetricsResponse]
          expected = FullMetricsResponse(
            label = "2024",
            metrics = MetricsResponse(
              List("Jan", "Feb", "Mar"),
              List(100, 300, 600),
              List(10f, 30f, 60f),
              List(10f, 30f, 60f),
              List(10f, 30f, 60f)
            )
          )
        yield assertTrue(
          response.status.code === Status.Ok.code,
          actual === expected
        )
      }
    ).provideSomeLayer(setupDbLayer)
end ProgressChartSpec
