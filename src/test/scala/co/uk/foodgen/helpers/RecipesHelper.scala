package co.uk.foodgen.helpers

import cats.effect.IO
import co.uk.foodgen.models.MealType.Breakfast
import co.uk.foodgen.models.{DietaryRequirement, MealType, Recipe}
import co.uk.foodgen.service.RecipeService

object RecipesHelper:

  def addRecipe(
    name: String,
    mealType: MealType = Breakfast,
    description: Option[String] = None,
    imageUrl: String = "",
    calories: Int = 0,
    carbohydrates: Float = 0,
    proteins: Float = 0,
    fats: Float = 0,
    diets: List[DietaryRequirement] = Nil
  )(using recipeService: RecipeService): IO[Recipe.Id] =
    val recipe = Recipe(
      id = Recipe.Id(0),
      name = name,
      mealType = mealType,
      description = description,
      imageUrl = imageUrl,
      calories = calories,
      carbohydrates = carbohydrates,
      proteins = proteins,
      fats = fats,
      diets = diets
    )
    recipeService.createRecipe(recipe)

end RecipesHelper
