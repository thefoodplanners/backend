package co.uk.foodgen.services

import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.{DietaryRequirement, MealType, Recipe, User}
import co.uk.foodgen.repositories.{RecipeRepository, UserRepository}
import co.uk.foodgen.services.models.MacrosFilter

object RecipeService:
  def createRecipe(
    name: String,
    mealType: MealType,
    description: Option[String],
    imageUrl: String,
    calories: Int,
    carbohydrates: Float,
    proteins: Float,
    fats: Float,
    diets: List[DietaryRequirement]
  ) =
    val recipe =
      Recipe(EntityId(0), name, mealType, description, imageUrl, calories, carbohydrates, proteins, fats, diets)
    RecipeRepository.insertRecipe(recipe)

  def search(
    query: Option[String],
    calories: Option[Int],
    carbohydrates: Option[Float],
    proteins: Option[Float],
    fats: Option[Float],
    dietsFilterStr: List[String],
    limit: Int,
    offset: Int
  ) =
    val macrosFilter = MacrosFilter(calories, carbohydrates, proteins, fats)
    val dietsFilter = dietsFilterStr.map(s => DietaryRequirement.valueOf(s.capitalize))
    RecipeRepository.selectRecipesFilter(query, macrosFilter, dietsFilter, limit, offset)

  def recommendations(userId: EntityId[User]) =
    for
      user <- UserRepository.selectUserByIdOrFail(userId)
      macrosFilter = MacrosFilter.empty.copy(calories = user.targetCalories)
      recipes <- RecipeRepository.selectRecipesFilter(
        macrosFilter = macrosFilter,
        dietsFilter = user.dietaryRequirements,
        limit = 3,
        offset = 0
      )
    yield recipes

end RecipeService
