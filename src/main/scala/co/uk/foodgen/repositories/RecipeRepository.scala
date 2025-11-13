package co.uk.foodgen.repositories

import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.{DietaryRequirement, Recipe}
import co.uk.foodgen.services.models.MacrosFilter
import io.getquill.*
import io.getquill.context.ZioJdbc.QIO
import zio.*

import java.sql.SQLException

object RecipeRepository:
  import DbContext.*

  def insertRecipe(recipe: Recipe): QIO[EntityId[Recipe]] =
    run(query[Recipe].insertValue(lift(recipe)).returningGenerated(_.id))

  def selectRecipeById(recipeId: EntityId[Recipe]): QIO[Option[Recipe]] =
    run(query[Recipe].filter(_.id == lift(recipeId)).value)

  def selectRecipeByIdOrFail(recipeId: EntityId[Recipe]): QIO[Recipe] =
    selectRecipeById(recipeId).expectOne

  def selectRecipesFilter(
    strQuery: Option[String] = None,
    macrosFilter: MacrosFilter = MacrosFilter.empty,
    dietsFilter: List[DietaryRequirement] = Nil,
    limit: Int,
    offset: Int
  ): QIO[List[Recipe]] = run(
    query[Recipe]
      .filter(r => lift(strQuery).filterIfDefined(r.name.like))
      .filter(r => lift(macrosFilter.calories).filterIfDefined(r.calories <= _))
      .filter(r => lift(macrosFilter.carbohydrates).filterIfDefined(r.carbohydrates <= _))
      .filter(r => lift(macrosFilter.proteins).filterIfDefined(r.proteins <= _))
      .filter(r => lift(macrosFilter.fats).filterIfDefined(r.fats <= _))
      .filter(r => infix"${r.dietaryRequirements} @> ${lift(dietsFilter)}".as[Boolean])
      .sortBy(_ => infix"RANDOM()".as[Float])
      .drop(lift(offset))
      .take(lift(limit))
  )

end RecipeRepository
