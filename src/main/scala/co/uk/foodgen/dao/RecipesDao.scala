package co.uk.foodgen.dao

import co.uk.foodgen.models.{DietaryRequirement, MacrosFilter, Recipe}
import doobie.implicits.toSqlInterpolator
import doobie.util.fragments.*
import doobie.{ConnectionIO, Fragment}

object RecipesDao:
  private val selectRecipe =
    fr"""
    select
      id,
      name,
      meal_type::meal_type,
      description,
      image_url,
      calories,
      carbohydrates,
      proteins,
      fats,
      dietary_requirements::diet[]
    from recipes
  """

  def getRecipe(recipeId: Recipe.Id): ConnectionIO[Recipe] =
    (selectRecipe ++ fr"where id = $recipeId")
      .query[Recipe]
      .unique

  def insertRecipe(recipe: Recipe): ConnectionIO[Recipe.Id] =
    sql"""
         insert into recipes (
           name,
           meal_type,
           description,
           image_url,
           calories,
           carbohydrates,
           proteins,
           fats,
           dietary_requirements
        )
         values (
           ${recipe.name},
           ${recipe.mealType},
           ${recipe.description},
           ${recipe.imageUrl},
           ${recipe.calories},
           ${recipe.carbohydrates},
           ${recipe.proteins},
           ${recipe.fats},
           ${recipe.diets}::diet[]
         )""".update
      .withUniqueGeneratedKeys[Recipe.Id]("id")

  def selectRecipesFilter(
    query: Option[String] = None,
    macrosFilter: MacrosFilter = MacrosFilter.empty,
    dietsFilter: List[DietaryRequirement] = Nil,
    limit: Int,
    offset: Int
  ): ConnectionIO[List[Recipe]] =
    val queryWhere = query.map(q => fr"name like ${q + "%"}")
    val calories = macrosFilter.maxCalories.map(ca => fr"calories <= $ca")
    val carbs = macrosFilter.maxCarbs.map(cb => fr"carbohydrates <= $cb")
    val proteins = macrosFilter.maxProteins.map(p => fr"proteins <= $p")
    val fats = macrosFilter.maxFats.map(f => fr"fats <= $f")
    val diets = fr"dietary_requirements @> $dietsFilter::diet[]"
    val conditions = whereAndOpt(
      queryWhere,
      Some(diets),
      calories,
      carbs,
      proteins,
      fats
    )

    (
      selectRecipe ++
        conditions ++
        fr0"limit $limit offset $offset"
    )
      .query[Recipe]
      .to[List]
