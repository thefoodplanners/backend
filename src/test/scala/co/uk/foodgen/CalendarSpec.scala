package co.uk.foodgen

import AuthenticationHelper.loginUser
import RecipesHelper.addRecipe
import co.uk.foodgen.payload.responses.MealResponses.MealsResponse
import co.uk.foodgen.{httpApp, removeFields, setupDbLayer}
import co.uk.foodgen.api.{AuthMiddleware, toResponseError}
import co.uk.foodgen.domain.Period
import co.uk.foodgen.payload.requests.CreateMealRequest
import zio.ZIO
import zio.http.*
import zio.json.EncoderOps
import zio.prelude.EqualOps
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec
import zio.test.*

import java.time.LocalDate
import javax.sql.DataSource
import scala.language.strictEquality

object CalendarSpec extends ZIOSpecDefault:
  private val date = LocalDate.parse("2024-01-01")

  def spec =
    suite("Calendar")(
      test("Should append the meal when calendar is empty") {
        for
          recipeId <- RecipesHelper.addRecipe("recipe1")
          authToken <- loginUser()
          requestBody = CreateMealRequest(date, mealNumber = None, recipeId)
          request = Request.post("/calendar/meals", Body.from(requestBody)).addHeader(authToken)
          response <- httpApp(request)
        yield assertTrue(response.status.code === Status.Created.code)
      },
      test("Should append the meal to a populated calendar") {
        for
          recipeId <- RecipesHelper.addRecipe("recipe1")
          authToken <- loginUser()
          userId <- AuthMiddleware.decodeAuthToken(authToken)
          _ <- MealsHelper.addMeal(date, mealNumber = None, recipeId, userId)

          requestBody = CreateMealRequest(date, mealNumber = None, recipeId)
          request = Request.post("/calendar/meals", Body.from(requestBody)).addHeader(authToken)
          response <- httpApp(request)
          meals <- MealsHelper.getMeals(date, Period.Day, userId)
          actual <- ZIO
            .fromEither(meals.toJsonAST.map(_.removeFields("day", "recipe")))
            .mapError(Exception(_))
            .toResponseError
          expected <- ResourceFileReader.readJsonFile("calendar/mealsAppendCalendar.json")
        yield assertTrue(
          response.status.code === Status.Created.code,
          actual == expected
        )
      },
      test("Should add the meal to a populated calendar") {
        for
          recipeId <- RecipesHelper.addRecipe("recipe1")
          authToken <- loginUser()
          userId <- AuthMiddleware.decodeAuthToken(authToken)
          _ <- MealsHelper.addMeal(date, mealNumber = None, recipeId, userId)
          _ <- MealsHelper.addMeal(date, mealNumber = None, recipeId, userId)
          _ <- MealsHelper.addMeal(date, mealNumber = Some(4), recipeId, userId)

          requestBody = CreateMealRequest(date, mealNumber = Some(3), recipeId)
          request = Request.post("/calendar/meals", Body.from(requestBody)).addHeader(authToken)
          response <- httpApp(request)
          meals <- MealsHelper.getMeals(date, Period.Day, userId)
          actual <- ZIO
            .fromEither(meals.toJsonAST.map(_.removeFields("day", "recipe")))
            .mapError(Exception(_))
            .toResponseError
          expected <- ResourceFileReader.readJsonFile("calendar/mealsAddCalendar.json")
        yield assertTrue(
          response.status.code === Status.Created.code,
          actual == expected
        )
      },
      test("Should fetch all the meals for a given week") {
        for
          recipeId <- RecipesHelper.addRecipe("recipe1")
          authToken <- loginUser()
          userId <- AuthMiddleware.decodeAuthToken(authToken)
          _ <- MealsHelper.addMeal(date, mealNumber = None, recipeId, userId)
          _ <- MealsHelper.addMeal(date, mealNumber = None, recipeId, userId)
          _ <- MealsHelper.addMeal(date = date.plusDays(1), mealNumber = None, recipeId, userId)
          _ <- MealsHelper.addMeal(date = date.plusDays(2), mealNumber = None, recipeId, userId)
          _ <- MealsHelper.addMeal(date = date.plusDays(3), mealNumber = None, recipeId, userId)
          _ <- MealsHelper.addMeal(date = date.plusDays(4), mealNumber = None, recipeId, userId)
          _ <- MealsHelper.addMeal(date = date.plusDays(5), mealNumber = None, recipeId, userId)
          _ <- MealsHelper.addMeal(date = date.plusDays(6), mealNumber = None, recipeId, userId)

          url <- ZIO.fromEither(URL.decode(s"/calendar/meals/week?date=$date")).toResponseError
          request = Request.get(url).addHeader(authToken)
          response <- httpApp(request)
          result <- response.bodyAs[MealsResponse]
          actual <- ZIO
            .fromEither(result.toJsonAST.map(_.removeFields("day", "recipe")))
            .mapError(Exception(_))
            .toResponseError
          expected <- ResourceFileReader.readJsonFile("calendar/mealsResponseForWeek.json")
        yield assertTrue(
          response.status.code === Status.Ok.code,
          actual == expected
        )
      },
      test("Should update meal with new recipe") {
        for
          recipeId1 <- RecipesHelper.addRecipe("recipe1")
          recipeId2 <- RecipesHelper.addRecipe("recipe2")
          authToken <- loginUser()
          userId <- AuthMiddleware.decodeAuthToken(authToken)
          mealId <- MealsHelper.addMeal(date, mealNumber = None, recipeId1, userId)
          url <- ZIO.fromEither(URL.decode(s"/calendar/meals/$mealId?recipe-id=$recipeId2")).toResponseError
          request = Request.put(url, Body.empty).addHeader(authToken)
          response <- httpApp(request)
          actual <- MealsHelper.getMeal(mealId, userId)
        yield assertTrue(
          response.status.code === Status.NoContent.code,
          actual.recipeId === recipeId2
        )
      },
      test("Should move meal to another position in the calendar") {
        for
          recipeId <- RecipesHelper.addRecipe("recipe1")
          authToken <- loginUser()
          userId <- AuthMiddleware.decodeAuthToken(authToken)
          mealId1 <- MealsHelper.addMeal(date = date, mealNumber = None, recipeId, userId)
          mealId2 <- MealsHelper.addMeal(date = date, mealNumber = None, recipeId, userId)
          mealId3 <- MealsHelper.addMeal(date = date, mealNumber = None, recipeId, userId)
          mealId4 <- MealsHelper.addMeal(date = date.plusDays(1), mealNumber = None, recipeId, userId)
          mealId5 <- MealsHelper.addMeal(date = date.plusDays(1), mealNumber = None, recipeId, userId)
          mealId6 <- MealsHelper.addMeal(date = date.plusDays(1), mealNumber = None, recipeId, userId)
          url <- ZIO
            .fromEither(URL.decode(s"/calendar/meals/$mealId2/move?date=${date.plusDays(1)}&meal-number=2"))
            .toResponseError
          request = Request.put(url, Body.empty).addHeader(authToken)
          response <- httpApp(request)
          result <- MealsHelper.getMealViews(date, Period.Week, userId)
          actual = result.map(mv => (mv.id, mv.mealNumber, mv.date))
          expected = List(
            (mealId1, 1, date),
            (mealId3, 2, date),
            (mealId4, 1, date.plusDays(1)),
            (mealId2, 2, date.plusDays(1)),
            (mealId5, 3, date.plusDays(1)),
            (mealId6, 4, date.plusDays(1))
          )
        yield assertTrue(
          response.status.code === Status.NoContent.code,
          result.length === 6,
          actual === expected
        )
      },
      test("Should delete meal from calendar and update accordingly") {
        for
          recipeId <- RecipesHelper.addRecipe("recipe1")
          authToken <- loginUser()
          userId <- AuthMiddleware.decodeAuthToken(authToken)
          mealId1 <- MealsHelper.addMeal(date = date, mealNumber = None, recipeId = recipeId, userId = userId)
          mealId2 <- MealsHelper.addMeal(date = date, mealNumber = None, recipeId = recipeId, userId = userId)
          mealId3 <- MealsHelper.addMeal(date = date, mealNumber = None, recipeId = recipeId, userId = userId)
          mealId4 <- MealsHelper.addMeal(date = date, mealNumber = None, recipeId = recipeId, userId = userId)
          url <- ZIO.fromEither(URL.decode(s"/calendar/meals/$mealId2")).toResponseError
          request = Request.delete(url).addHeader(authToken)
          response <- httpApp(request)
          result <- MealsHelper.getMealViews(date, Period.Week, userId)
          actual = result.map(mv => (mv.id, mv.mealNumber, mv.date))
          expected = List(
            (mealId1, 1, date),
            (mealId3, 3, date),
            (mealId4, 4, date)
          )
        yield assertTrue(
          response.status.code === Status.NoContent.code,
          result.length === 3,
          actual === expected
        )
      }
    ).provideSomeLayer(setupDbLayer)
end CalendarSpec
