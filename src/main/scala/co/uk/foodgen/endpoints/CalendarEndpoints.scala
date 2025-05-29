package co.uk.foodgen.endpoints

import cats.data.Ior
import cats.effect.IO
import co.uk.foodgen.models.{Meal, Recipe}
import co.uk.foodgen.payload.{CreateMealRequest, MealsResponse}
import co.uk.foodgen.service.MealService
import co.uk.foodgen.service.models.Period
import doobie.util.transactor.Transactor
import io.scalaland.chimney.dsl.transformInto
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.*

import java.time.LocalDate

class CalendarEndpoints(transactor: Transactor[IO]) extends HttpEndpoint:

  private lazy val mealService = new MealService(using transactor)

  private val getCalendarMeals = endpoint.get
    .in(calendarMealsUrl / path[Period]("period"))
    .in(query[LocalDate]("date"))
    .contextIn[AuthInfo]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(jsonBody[MealsResponse])
    .serverLogicSuccess[IO] { (period, date, userId, _) =>
      mealService
        .getAllMealsByPeriod(
          date = date,
          period = period,
          userId = userId
        )
        .map(MealsResponse(_, date))
    }

  private val createCalendarMeal = endpoint.post
    .in(calendarMealsUrl)
    .in(jsonBody[CreateMealRequest])
    .contextIn[AuthInfo]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(statusCode(StatusCode.Created))
    .serverLogicSuccess[IO] { (request, userId, _) =>
      mealService
        .saveMeal(
          date = request.date,
          mealNumber = request.mealNumber,
          recipeId = request.recipeId,
          userId = userId
        )
        .void
    }

  private val updateCalendarMeal = endpoint.put
    .in(calendarMealsUrl / path[Meal.Id]("meal-id"))
    .in(query[Recipe.Id]("recipe-id"))
    .contextIn[AuthInfo]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(statusCode(StatusCode.NoContent))
    .serverLogicSuccess[IO] { (mealId, recipeId, userId, _) =>
      mealService.updateMealWithNewRecipe(
        mealId = mealId,
        recipeId = recipeId,
        userId = userId
      )
    }

  private val moveCalendarMeal = endpoint.put
    .in(calendarMealsUrl / path[Meal.Id]("meal-id") / "move")
    .in(query[LocalDate]("date"))
    .in(query[Int]("meal-number"))
    .contextIn[AuthInfo]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(statusCode(StatusCode.NoContent))
    .serverLogicSuccess[IO] { (mealId, newDate, newMealNumber, userId, _) =>
      mealService.moveMeal(
        mealId = mealId,
        newDate = newDate,
        newMealNumber = newMealNumber,
        userId = userId
      )
    }

  private val deleteCalendarMeal = endpoint.delete
    .in(calendarMealsUrl / path[Meal.Id]("meal-id"))
    .contextIn[AuthInfo]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(statusCode(StatusCode.NoContent))
    .serverLogicSuccess[IO] { (mealId, userId, _) =>
      mealService.removeMeal(
        mealId = mealId,
        userId = userId
      )
    }

  val endpoints = List(
    getCalendarMeals,
    createCalendarMeal,
    updateCalendarMeal,
    moveCalendarMeal,
    deleteCalendarMeal
  )

  val routes =
    val authRoutes = Http4sServerInterpreter[IO]().toContextRoutes(endpoints)
    Ior.right(authRoutes)

end CalendarEndpoints
