package co.uk.foodgen.dao

import co.uk.foodgen.models.{Meal, Recipe, User}
import co.uk.foodgen.service.models.{MealView, Period}
import doobie.implicits.toSqlInterpolator
import doobie.postgres.implicits.JavaLocalDateMeta
import doobie.{ConnectionIO, Fragment}

import java.time.LocalDate

object MealsDao:

  def selectMeal(mealId: Meal.Id, userId: User.Id): ConnectionIO[Meal] =
    sql"select * from meals where id = $mealId and user_id = $userId"
      .query[Meal]
      .unique

  def selectLatestMealNumberFromMeal(date: LocalDate, userId: User.Id): ConnectionIO[Int] =
    sql"select max(meal_number) from meals where user_id = $userId and date = $date"
      .query[Option[Int]]
      .unique
      .map(_.fold(1)(_ + 1))

  def insertMeal(date: LocalDate, mealNumber: Int, recipeId: Recipe.Id, userId: User.Id): ConnectionIO[Meal.Id] =
    sql"insert into meals (user_id, date, meal_number, recipe_id) values ($userId, $date, $mealNumber, $recipeId)".update
      .withUniqueGeneratedKeys[Meal.Id]("id")

  def updateMeal(mealId: Meal.Id, recipeId: Recipe.Id, userId: User.Id): ConnectionIO[Int] =
    sql"update meals set recipe_id = $recipeId where id = $mealId and user_id = $userId".update.run

  def moveMeal(mealId: Meal.Id, date: LocalDate, mealNumber: Int, userId: User.Id): ConnectionIO[Int] =
    sql"update meals set date = $date, meal_number = $mealNumber where id = $mealId and user_id = $userId".update.run

  def deleteMeal(mealId: Meal.Id, userId: User.Id): ConnectionIO[Int] =
    sql"delete from meals where id = $mealId and user_id = $userId".update.run

  def syncMealNumbers(
    userId: User.Id,
    date: LocalDate,
    mealNumber: Int,
    isDeletion: Boolean
  ): ConnectionIO[Int] =
    val operator = if isDeletion then Fragment.const("- 1") else Fragment.const("+ 1")
    (fr"update meals set meal_number = meal_number" ++ operator ++
      fr"""where user_id = $userId and
            date = $date and
            meal_number >= $mealNumber
          """).update.run

  def selectMealsByPeriod(date: LocalDate, period: Period, userId: User.Id): ConnectionIO[List[MealView]] =
    val periodSql = Fragment.const0(period.toString)

    fr"""
         select
           m.id,
           date,
           meal_number,
           r.id,
           name,
           meal_type,
           description,
           image_url,
           calories,
           carbohydrates,
           proteins,
           fats,
           dietary_requirements::diet[]
         from meals m
         join recipes r on m.recipe_id = r.id
         where
           extract('$periodSql' from date) = extract('$periodSql' from $date) and
           user_id = $userId
         order by date, meal_number
           """
      .query[MealView]
      .to[List]
