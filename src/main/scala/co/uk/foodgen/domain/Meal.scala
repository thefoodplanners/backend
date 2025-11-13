package co.uk.foodgen.domain

import io.getquill.{InsertMeta, insertMeta}

import java.time.LocalDate

import Entity.EntityId

case class Meal(
  id: EntityId[Meal],
  userId: EntityId[User],
  date: LocalDate,
  mealNumber: Int,
  recipeId: EntityId[Recipe]
)

object Meal:
  inline given InsertMeta[Meal] = insertMeta(_.id)
