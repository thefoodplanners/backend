package co.uk.foodgen.repositories

import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.{Meal, MealView, Period, Recipe, User, *}
import io.getquill.*
import io.getquill.context.ZioJdbc.QIO
import zio.*

import java.sql.SQLException
import java.time.LocalDate

object MealRepository:
  import DbContext.*

  def selectMealById(mealId: EntityId[Meal], userId: EntityId[User]): QIO[Option[Meal]] =
    run(query[Meal].filter(m => m.id == lift(mealId) && m.userId == lift(userId)).value)

  def selectMealByIdOrFail(mealId: EntityId[Meal], userId: EntityId[User]): QIO[Meal] =
    selectMealById(mealId, userId).expectOne

  def selectLatestMealNumberFromMeal(date: LocalDate, userId: EntityId[User]): QIO[Int] =
    run(query[Meal].filter(m => m.date == lift(date) && m.userId == lift(userId)).map(_.mealNumber).max)
      .map(_.fold(1)(_ + 1))

  def insertMeal(meal: Meal): QIO[EntityId[Meal]] =
    run(query[Meal].insertValue(lift(meal)).returningGenerated(_.id))

  def updateMeal(
    mealId: EntityId[Meal],
    newRecipeId: EntityId[Recipe],
    userId: EntityId[User]
  ): QIO[Unit] = run(
    query[Meal]
      .filter(m => m.id == lift(mealId) && m.userId == lift(userId))
      .update(_.recipeId -> lift(newRecipeId))
  ).unit

  def moveMeal(mealId: EntityId[Meal], newDate: LocalDate, newMealNumber: Int, userId: EntityId[User]): QIO[Unit] =
    run(
      query[Meal]
        .filter(m => m.id == lift(mealId) && m.userId == lift(userId))
        .update(m => m.date -> lift(newDate), m => m.mealNumber -> lift(newMealNumber))
    ).unit

  def deleteMeal(mealId: EntityId[Meal], userId: EntityId[User]): QIO[Unit] =
    run(
      query[Meal]
        .filter(m => m.id == lift(mealId) && m.userId == lift(userId))
        .delete
    ).unit

  def syncMealNumbers(userId: EntityId[User], date: LocalDate, mealNumber: Int, isDeletion: Boolean): QIO[Unit] =
    run(
      query[Meal]
        .filter(m => m.userId == lift(userId) && m.date == lift(date) && m.mealNumber >= lift(mealNumber))
        .update(m => m.mealNumber -> (if lift(isDeletion) then m.mealNumber - 1 else m.mealNumber + 1))
    ).unit

  def selectMealsByPeriod(date: LocalDate, period: Period, userId: EntityId[User]): QIO[List[MealView]] =
    val periodStr = period.toString
    run(
      query[Meal]
        .join(query[Recipe])
        .on(_.recipeId == _.id)
        .filter((m, _) =>
          infix"DATE_TRUNC(${lift(periodStr)}, ${m.date}) = DATE_TRUNC(${lift(periodStr)}, ${lift(date)})".asCondition &&
            m.userId == lift(userId)
        )
        .sortBy((m, _) => (m.date, m.mealNumber))
        .map((m, r) =>
          MealView(
            id = m.id,
            date = m.date,
            mealNumber = m.mealNumber,
            recipe = r
          )
        )
    )

end MealRepository
