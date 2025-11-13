package co.uk.foodgen.api

import co.uk.foodgen.api.endpoints.RecipeEndpoints.*
import co.uk.foodgen.services.RecipeService
import zio.ZIO
import zio.http.*
import zio.schema.{DeriveSchema, Schema, derived}

import javax.sql.DataSource

object RecipeRoutes extends APIRoute:
  override val routes = Routes(
    getRecipesSearch.implement { (params: SearchParams) =>
      (for
        _ <- ZIO.service[AuthInfo]
        SearchParams(query, calories, carbohydrates, proteins, fats, diets, limit, offset) = params
        recipes <- RecipeService.search(query, calories, carbohydrates, proteins, fats, diets, limit, offset)
        responseBody = recipes
      yield responseBody).toRoutesError
    },
    getRecipeRecommendations.implement { _ =>
      (for
        userId <- ZIO.service[AuthInfo]
        recipes <- RecipeService.recommendations(userId)
        responseBody = recipes
      yield responseBody).toRoutesError
    }
  )

end RecipeRoutes
