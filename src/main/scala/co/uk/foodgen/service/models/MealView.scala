package co.uk.foodgen.service.models

import co.uk.foodgen.models.{Meal, Recipe}
import doobie.postgres.implicits.JavaLocalDateMeta
import doobie.util.Read

import java.time.LocalDate

case class MealView(
  id: Meal.Id,
  date: LocalDate,
  mealNumber: Int,
  recipe: Recipe
)

object MealView:
  given Read[MealView] = Read[
    (
      Meal.Id,
      LocalDate,
      Int,
      Recipe
    )
  ].map(MealView.apply _ tupled _)
