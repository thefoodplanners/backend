package co.uk.foodgen.endpoints

import cats.effect.IO
import co.uk.foodgen.models.{Meal, Recipe, User}
import co.uk.foodgen.payload.{CreateMealRequest, MealsResponse}
import co.uk.foodgen.service.MealService
import co.uk.foodgen.service.models.Period
import doobie.util.transactor.Transactor
import io.scalaland.chimney.dsl.transformInto
import org.http4s.ContextRoutes
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.*

import java.time.LocalDate

class CalendarEndpoints(transactor: Transactor[IO]):

  private lazy val mealService = new MealService(using transactor)

  private val getCalendarMeals = endpoint.get
    .in(calendarMealsUrl / path[Period]("period"))
    .in(query[LocalDate]("date"))
    .contextIn[User.Id]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(jsonBody[MealsResponse])
    .serverLogicSuccess[IO] { case (period, date, userId) =>
      mealService
        .getAllMealsByPeriod(
          date = date,
          period = period,
          userId = userId
        )
        .map(_.transformInto[MealsResponse])
    }

  private val createCalendarMeal = endpoint.post
    .in(calendarMealsUrl)
    .in(jsonBody[CreateMealRequest])
    .contextIn[User.Id]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(statusCode(StatusCode.Created))
    .serverLogicSuccess[IO] { (request, userId) =>
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
    .contextIn[User.Id]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(statusCode(StatusCode.NoContent))
    .serverLogicSuccess[IO] { (mealId, recipeId, userId) =>
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
    .contextIn[User.Id]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(statusCode(StatusCode.NoContent))
    .serverLogicSuccess[IO] { (mealId, newDate, newMealNumber, userId) =>
      mealService.moveMeal(
        mealId = mealId,
        newDate = newDate,
        newMealNumber = newMealNumber,
        userId = userId
      )
    }

  private val deleteCalendarMeal = endpoint.delete
    .in(calendarMealsUrl / path[Meal.Id]("meal-id"))
    .contextIn[User.Id]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(statusCode(StatusCode.NoContent))
    .serverLogicSuccess[IO] { (mealId, userId) =>
      mealService.removeMeal(
        mealId = mealId,
        userId = userId
      )
    }

  val allEndpoints = List(
    getCalendarMeals,
    createCalendarMeal,
    updateCalendarMeal,
    moveCalendarMeal,
    deleteCalendarMeal
  )

  val allRoutes: ContextRoutes[User.Id, IO] = Http4sServerInterpreter[IO]().toContextRoutes(allEndpoints)

end CalendarEndpoints
