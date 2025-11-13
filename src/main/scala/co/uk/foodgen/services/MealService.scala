package co.uk.foodgen.services

import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.{Meal, MealView, Period, Recipe, User, *}
import co.uk.foodgen.repositories.MealRepository
import io.getquill.context.ZioJdbc.QIO
import zio.{RIO, ZIO}

import java.time.LocalDate
import javax.sql.DataSource

object MealService:
  import co.uk.foodgen.repositories.DbContext.*

  def saveMeal(
    date: LocalDate,
    mealNumber: Option[Int],
    recipeId: EntityId[Recipe],
    userId: EntityId[User]
  ): QIO[EntityId[Meal]] =
    for
      fetchedMealNumber <- mealNumber.fold(MealRepository.selectLatestMealNumberFromMeal(date, userId))(ZIO.succeed)
      meal = Meal(EntityId(0), userId, date, fetchedMealNumber, recipeId)
      mealId <- MealRepository.insertMeal(meal)
    yield mealId

  def updateMealWithNewRecipe(mealId: EntityId[Meal], recipeId: EntityId[Recipe], userId: EntityId[User]): QIO[Unit] =
    MealRepository.updateMeal(mealId, recipeId, userId)

  def removeMeal(mealId: EntityId[Meal], userId: EntityId[User]): QIO[Unit] =
    MealRepository.deleteMeal(mealId, userId)

  def moveMeal(
    mealId: EntityId[Meal],
    newDate: LocalDate,
    newMealNumber: Int,
    userId: EntityId[User]
  ): RIO[DataSource, Unit] =
    transaction {
      for
        meal <- MealRepository.selectMealByIdOrFail(mealId, userId)
        _ <- MealRepository.syncMealNumbers(meal.userId, newDate, newMealNumber, isDeletion = false)
        _ <- MealRepository.moveMeal(mealId, newDate, newMealNumber, userId)
        _ <- MealRepository.syncMealNumbers(meal.userId, meal.date, meal.mealNumber, isDeletion = true)
      yield ()
    }

  def getMeal(id: EntityId[Meal], userId: EntityId[User]): QIO[Meal] =
    MealRepository.selectMealByIdOrFail(id, userId)

  def getAllMealsByPeriod(date: LocalDate, period: Period, userId: EntityId[User]): QIO[List[MealView]] =
    MealRepository.selectMealsByPeriod(date, period, userId)

end MealService
