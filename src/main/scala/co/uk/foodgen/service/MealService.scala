package co.uk.foodgen.service

import cats.effect.IO
import cats.syntax.applicative.catsSyntaxApplicativeId
import co.uk.foodgen.dao.MealsDao
import co.uk.foodgen.models.{Meal, Recipe, User}
import co.uk.foodgen.service.models.{MealView, Period}
import doobie.ConnectionIO
import doobie.util.transactor.Transactor

import java.time.LocalDate

final class MealService(using Transactor[IO]):

  def saveMeal(
    date: LocalDate,
    mealNumber: Option[Int],
    recipeId: Recipe.Id,
    userId: User.Id
  ): IO[Meal.Id] =
    (for
      fetchedMealNumber <- mealNumber.fold(
        MealsDao.selectLatestMealNumberFromMeal(date, userId)
      )(_.pure[ConnectionIO])
      _ <- MealsDao.syncMealNumbers(userId, date, fetchedMealNumber, false)
      mealId <- MealsDao.insertMeal(date, fetchedMealNumber, recipeId, userId)
    yield mealId).tx

  def updateMealWithNewRecipe(mealId: Meal.Id, recipeId: Recipe.Id, userId: User.Id): IO[Unit] =
    MealsDao.updateMeal(mealId, recipeId, userId).tx.void

  def removeMeal(mealId: Meal.Id, userId: User.Id): IO[Unit] =
    (for
      meal <- MealsDao.selectMeal(mealId, userId)
      _ <- MealsDao.deleteMeal(mealId, userId)
      _ <- MealsDao.syncMealNumbers(meal.userId, meal.date, meal.mealNumber, isDeletion = true)
    yield ()).tx

  def moveMeal(mealId: Meal.Id, newDate: LocalDate, newMealNumber: Int, userId: User.Id): IO[Unit] =
    (for
      meal <- MealsDao.selectMeal(mealId, userId)
      _ <- MealsDao.syncMealNumbers(meal.userId, newDate, newMealNumber, isDeletion = false)
      _ <- MealsDao.moveMeal(mealId, newDate, newMealNumber, userId)
      _ <- MealsDao.syncMealNumbers(meal.userId, meal.date, meal.mealNumber, isDeletion = true)
    yield ()).tx

  def getMeal(id: Meal.Id, userId: User.Id): IO[Meal] = MealsDao.selectMeal(id, userId).tx

  def getAllMealsByPeriod(date: LocalDate, period: Period, userId: User.Id): IO[List[MealView]] =
    MealsDao.selectMealsByPeriod(date, period, userId).tx
