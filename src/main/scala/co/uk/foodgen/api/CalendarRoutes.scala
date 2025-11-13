package co.uk.foodgen.api

import co.uk.foodgen.api.endpoints.CalendarEndpoints.*
import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.{Meal, Period, Recipe}
import co.uk.foodgen.payload.requests.CreateMealRequest
import co.uk.foodgen.payload.responses.MealResponses.MealsResponse
import co.uk.foodgen.services.MealService
import zio.ZIO
import zio.http.*
import zio.schema.{DeriveSchema, Schema, derived}

import java.time.LocalDate
import javax.sql.DataSource

object CalendarRoutes extends APIRoute:
  override val routes = Routes(
    getCalendarMealsPeriod.implement { (period: Period, date: LocalDate) =>
      (for
        userId <- ZIO.service[AuthInfo]
        mealViews <- MealService.getAllMealsByPeriod(date, period, userId)
        responseBody = MealsResponse(mealViews, date)
      yield responseBody).toRoutesError
    },
    postCalendarMeals.implement { case CreateMealRequest(date, mealNumber, recipeId) =>
      (for
        userId <- ZIO.service[AuthInfo]
        _ <- MealService.saveMeal(date, mealNumber, recipeId, userId)
      yield ()).toRoutesError
    },
    putCalendarMealsId.implement { (mealId: EntityId[Meal], recipeId: EntityId[Recipe]) =>
      (for
        userId <- ZIO.service[AuthInfo]
        _ <- MealService.updateMealWithNewRecipe(mealId, recipeId, userId)
      yield ()).toRoutesError
    },
    putCalendarMealsIdMove.implement { (mealId: EntityId[Meal], date: LocalDate, mealNumber: Int) =>
      (for
        userId <- ZIO.service[AuthInfo]
        _ <- MealService.moveMeal(mealId, date, mealNumber, userId)
      yield ()).toRoutesError
    },
    deleteCalendarMealId.implement { (mealId: EntityId[Meal]) =>
      (for
        userId <- ZIO.service[AuthInfo]
        _ <- MealService.removeMeal(mealId, userId)
      yield ()).toRoutesError
    }
  )

end CalendarRoutes
