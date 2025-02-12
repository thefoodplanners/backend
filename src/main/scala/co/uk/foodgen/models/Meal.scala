package co.uk.foodgen.models

import doobie.postgres.implicits.JavaLocalDateMeta
import doobie.util.meta.Meta
import doobie.util.{Get, Read, Write}

import java.time.LocalDate

case class Meal(
  id: Meal.Id,
  userId: User.Id,
  date: LocalDate,
  mealNumber: Int,
  recipeId: Recipe.Id
)

object Meal extends HasId:
  given Write[Meal] = Write[
    (
      User.Id,
      LocalDate,
      Int,
      Recipe.Id
    )
  ].contramap { case Meal(_, userId, date, mealNumber, recipeId) =>
    (userId, date, mealNumber, recipeId)
  }

  given Read[Meal] = Read[
    (
      Meal.Id,
      User.Id,
      LocalDate,
      Int,
      Recipe.Id
    )
  ].map(Meal.apply _ tupled _)
