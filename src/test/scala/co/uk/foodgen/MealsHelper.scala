package co.uk.foodgen

import co.uk.foodgen.IOR
import co.uk.foodgen.api.toResponseError
import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.*
import co.uk.foodgen.payload.responses.MealResponses.{MealDaysResponse, MealsResponse}
import co.uk.foodgen.services.MealService

import java.time.LocalDate

object MealsHelper:
  def addMeal(
    date: LocalDate,
    mealNumber: Option[Int],
    recipeId: EntityId[Recipe],
    userId: EntityId[User]
  ): IOR[EntityId[Meal]] = MealService.saveMeal(date, mealNumber, recipeId, userId).toResponseError

  def getMeals(date: LocalDate, period: Period, userId: EntityId[User]): IOR[List[MealDaysResponse]] =
    MealService
      .getAllMealsByPeriod(date, period, userId)
      .map(MealsResponse(_, date))
      .toResponseError

  def getMealViews(date: LocalDate, period: Period, userId: EntityId[User]): IOR[List[MealView]] =
    MealService.getAllMealsByPeriod(date, period, userId).toResponseError

  def getMeal(mealId: EntityId[Meal], userId: EntityId[User]): IOR[Meal] =
    MealService.getMeal(mealId, userId).toResponseError
end MealsHelper
