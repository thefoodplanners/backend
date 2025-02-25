package co.uk.foodgen.service

import cats.effect.IO
import cats.syntax.option.catsSyntaxOptionId
import co.uk.foodgen.dao.{RecipesDao, UsersDao}
import co.uk.foodgen.models.{DietaryRequirement, MacrosFilter, Recipe, User}
import doobie.util.transactor.Transactor

final class RecipeService(using Transactor[IO]):
  def createRecipe(recipe: Recipe): IO[Recipe.Id] =
    RecipesDao.insertRecipe(recipe).tx

  def search(
    query: Option[String] = None,
    macrosFilter: MacrosFilter = MacrosFilter.empty,
    dietsFilter: List[DietaryRequirement] = Nil,
    limit: Int,
    offset: Int
  ): IO[List[Recipe]] =
    RecipesDao.selectRecipesFilter(query, macrosFilter, dietsFilter, limit, offset.some).tx

  def recommendations(userId: User.Id): IO[List[Recipe]] =
    (for
      userDiets <- UsersDao.selectDietaryRequirements(userId)
      recipes <- RecipesDao.selectRecipesFilter(dietsFilter = userDiets, limit = 3, offset = None)
    yield recipes).tx
