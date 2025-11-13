package co.uk.foodgen.domain

import java.time.LocalDate

import Entity.EntityId

case class MealView(
  id: EntityId[Meal],
  date: LocalDate,
  mealNumber: Int,
  recipe: Recipe
)
