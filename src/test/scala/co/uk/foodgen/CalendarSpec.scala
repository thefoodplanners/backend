package co.uk.foodgen

import cats.data.Kleisli
import cats.effect.IO
import io.circe.syntax.EncoderOps
import co.uk.foodgen.endpoints.*
import co.uk.foodgen.helpers.{LoginHelper, MealsHelper, RecipesHelper}
import co.uk.foodgen.models.Meal
import co.uk.foodgen.payload.CreateMealRequest
import co.uk.foodgen.service.models.Period
import co.uk.foodgen.service.{MealService, RecipeService}
import doobie.util.transactor.Transactor
import io.circe.Json
import org.http4s.Method.*
import org.http4s.Status.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.{HttpApp, Method, Request, RequestCookie, Response, Uri}

import java.time.LocalDate

object CalendarSpec extends IOSuite:
  private val app = (tx: Transactor[IO]) =>
    (
      Server.mainHttpRoutes(tx).orNotFound,
      new RecipeService(using tx),
      new MealService(using tx)
    )
  private def calendarRoutesTest = testWithDb(app)(name => expects => test(name)(expects))

  private val date = LocalDate.parse("2024-01-01")

  calendarRoutesTest("Should append the meal when calendar is empty") {
    case (router @ given HttpApp[IO], _ @ given RecipeService, _) =>
      for
        recipeId <- RecipesHelper.addRecipe("recipe1")
        loginToken @ given RequestCookie <- LoginHelper.loginUser()
        resource = CreateMealRequest(date, mealNumber = None, recipeId)
        request = Request[IO](POST, Uri.unsafeFromString(calendarMealsUrl.asString))
          .withEntity(resource)
          .addCookie(loginToken)
        response <- router(request)
        check = expect.eql(Created, response.status)
      yield check
  }

  calendarRoutesTest("Should append the meal to a populated calendar") {
    case (router @ given HttpApp[IO], _ @ given RecipeService, mealService) =>
      for
        recipeId <- RecipesHelper.addRecipe("recipe1")
        loginToken @ given RequestCookie <- LoginHelper.loginUser()
        userId <- decryptUserIdFromCookieOrFail(loginToken)
        mealId1 <- mealService.saveMeal(date, mealNumber = None, recipeId, userId)

        resource = CreateMealRequest(date, mealNumber = None, recipeId)
        request = Request[IO](POST, Uri.unsafeFromString(calendarMealsUrl.asString))
          .withEntity(resource)
          .addCookie(loginToken)
        response <- router(request)
        result <- MealsHelper.getMeals(date, Period.Day)
        actual = result.asJson
        expected <- ResourceFileReader.readJsonFile("calendar/mealsAppendCalendar.json")
        check = expect.eql(Created, response.status) and
          expect.eql(expected, actual.removeJsonFields(Set("day", "recipe")))
      yield check
  }

  calendarRoutesTest("Should add the meal to a populated calendar") {
    case (router @ given HttpApp[IO], _ @ given RecipeService, mealService) =>
      for
        recipeId <- RecipesHelper.addRecipe("recipe1")
        loginToken @ given RequestCookie <- LoginHelper.loginUser()
        userId <- decryptUserIdFromCookieOrFail(loginToken)
        mealId1 <- mealService.saveMeal(date, mealNumber = None, recipeId, userId)
        mealId2 <- mealService.saveMeal(date, mealNumber = None, recipeId, userId)
        mealId3 <- mealService.saveMeal(date, mealNumber = None, recipeId, userId)

        resource = CreateMealRequest(date, mealNumber = Some(2), recipeId)
        request = Request[IO](POST, Uri.unsafeFromString(calendarMealsUrl.asString))
          .withEntity(resource)
          .addCookie(loginToken)
        response <- router(request)
        result <- MealsHelper.getMeals(date, Period.Day)
        actual = result.asJson
        expected <- ResourceFileReader.readJsonFile("calendar/mealsAddCalendar.json")
        check = expect.eql(Created, response.status) and
          expect.eql(expected, actual.removeJsonFields(Set("day", "recipe")))
      yield check
  }

  calendarRoutesTest("Should fetch all the meals for a given week") {
    case (router @ given HttpApp[IO], _ @ given RecipeService, mealService) =>
      for
        recipeId <- RecipesHelper.addRecipe("recipe1")
        loginToken @ given RequestCookie <- LoginHelper.loginUser()
        userId <- decryptUserIdFromCookieOrFail(loginToken)
        mealId1 <- mealService.saveMeal(
          date = date,
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        mealId2 <- mealService.saveMeal(
          date = date,
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        mealId3 <- mealService.saveMeal(
          date = date.plusDays(1),
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        mealId4 <- mealService.saveMeal(
          date = date.plusDays(2),
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        mealId5 <- mealService.saveMeal(
          date = date.plusDays(3),
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        mealId6 <- mealService.saveMeal(
          date = date.plusDays(4),
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        mealId7 <- mealService.saveMeal(
          date = date.plusDays(5),
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        mealId8 <- mealService.saveMeal(
          date = date.plusDays(6),
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        request =
          Request[IO](GET, Uri.unsafeFromString(s"${calendarMealsUrl.asString}/week?date=$date"))
            .addCookie(loginToken)
        response <- router(request)
        result <- response.as[Json]
        expected <- ResourceFileReader.readJsonFile("calendar/mealsResponseForWeek.json")
        check = expect.eql(expected, result.removeJsonFields(Set("day", "recipe")))
      yield check
  }

  calendarRoutesTest("Should update meal with new recipe") {
    case (router @ given HttpApp[IO], _ @ given RecipeService, mealService) =>
      for
        recipeId1 <- RecipesHelper.addRecipe("recipe1")
        recipeId2 <- RecipesHelper.addRecipe("recipe2")
        loginToken @ given RequestCookie <- LoginHelper.loginUser()
        userId <- decryptUserIdFromCookieOrFail(loginToken)
        mealId <- mealService.saveMeal(
          date = date,
          mealNumber = None,
          recipeId = recipeId1,
          userId = userId
        )
        request = Request[IO](
          PUT,
          Uri.unsafeFromString(s"${calendarMealsUrl.asString}/$mealId?recipe-id=$recipeId2")
        )
          .addCookie(loginToken)
        response <- router(request)
        result <- mealService.getMeal(mealId, userId)
        check = expect.eql(NoContent, response.status) and
          expect.eql(recipeId2, result.recipeId)
      yield check
  }

  calendarRoutesTest("Should move meal to another position in the calendar") {
    case (router @ given HttpApp[IO], _ @ given RecipeService, mealService) =>
      for
        recipeId <- RecipesHelper.addRecipe("recipe1")
        loginToken @ given RequestCookie <- LoginHelper.loginUser()
        userId <- decryptUserIdFromCookieOrFail(loginToken)
        mealId1 <- mealService.saveMeal(
          date = date,
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        mealId2 <- mealService.saveMeal(
          date = date,
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        mealId3 <- mealService.saveMeal(
          date = date,
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        mealId4 <- mealService.saveMeal(
          date = date.plusDays(1),
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        mealId5 <- mealService.saveMeal(
          date = date.plusDays(1),
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        mealId6 <- mealService.saveMeal(
          date = date.plusDays(1),
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        request = Request[IO](
          PUT,
          Uri.unsafeFromString(
            s"${calendarMealsUrl.asString}/$mealId2/move?date=${date.plusDays(1)}&meal-number=2"
          )
        )
          .addCookie(loginToken)
        response <- router(request)
        result <- mealService.getAllMealsByPeriod(date, Period.Week, userId)
        expected = List(
          (mealId1, 1, date),
          (mealId3, 2, date),
          (mealId4, 1, date.plusDays(1)),
          (mealId2, 2, date.plusDays(1)),
          (mealId5, 3, date.plusDays(1)),
          (mealId6, 4, date.plusDays(1))
        )
        check = expect.eql(NoContent, response.status) and
          expect.eql(6, result.length) and
          expect.same(expected, result.map(mv => (mv.id, mv.mealNumber, mv.date)))
      yield check
  }

  calendarRoutesTest("Should delete meal from calendar and update accordingly") {
    case (router @ given HttpApp[IO], _ @ given RecipeService, mealService) =>
      for
        recipeId <- RecipesHelper.addRecipe("recipe1")
        loginToken @ given RequestCookie <- LoginHelper.loginUser()
        userId <- decryptUserIdFromCookieOrFail(loginToken)
        mealId1 <- mealService.saveMeal(
          date = date,
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        mealId2 <- mealService.saveMeal(
          date = date,
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        mealId3 <- mealService.saveMeal(
          date = date,
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        mealId4 <- mealService.saveMeal(
          date = date,
          mealNumber = None,
          recipeId = recipeId,
          userId = userId
        )
        request =
          Request[IO](DELETE, Uri.unsafeFromString(s"${calendarMealsUrl.asString}/$mealId2"))
            .addCookie(loginToken)
        response <- router(request)
        result <- mealService.getAllMealsByPeriod(date, Period.Week, userId)
        expected = Set(
          (mealId1, 1, date),
          (mealId3, 2, date),
          (mealId4, 3, date)
        )
        check = expect.eql(NoContent, response.status) and
          expect.eql(3, result.length) and
          expect.same(expected, result.map(mv => (mv.id, mv.mealNumber, mv.date)).toSet)
      yield check
  }

end CalendarSpec
