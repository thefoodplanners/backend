package co.uk.foodgen

import co.uk.foodgen.api.toResponseError
import co.uk.foodgen.domain.{DietaryRequirement, MealType}
import co.uk.foodgen.services.RecipeService

object RecipesHelper:
  def addRecipe(
    name: String,
    mealType: MealType = MealType.Breakfast,
    description: Option[String] = None,
    imageUrl: String = "imageurl.com",
    calories: Int = 100,
    carbohydrates: Float = 10,
    proteins: Float = 10,
    fats: Float = 10,
    diets: List[DietaryRequirement] = Nil
  ) = RecipeService
    .createRecipe(name, mealType, description, imageUrl, calories, carbohydrates, proteins, fats, diets)
    .toResponseError
end RecipesHelper
