package co.uk.foodgen.endpoints

import cats.data.Ior
import cats.effect.IO
import co.uk.foodgen.models.{DietaryRequirement, MacrosFilter, User}
import co.uk.foodgen.payload.RecipesResponse
import co.uk.foodgen.service.RecipeService
import doobie.util.transactor.Transactor
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.*

class RecipeEndpoints(transactor: Transactor[IO]) extends HttpEndpoint:

  private lazy val recipeService = new RecipeService(using transactor)

  private val recipesSearch = endpoint.get
    .in(recipeSearchUrl)
    .in(query[Option[String]]("query"))
    .in(query[List[String]]("diets"))
    .in(query[Option[Int]]("max-calories"))
    .in(query[Option[Float]]("max-carbohydrates"))
    .in(query[Option[Float]]("max-proteins"))
    .in(query[Option[Float]]("max-fats"))
    .in(query[Int]("limit"))
    .in(query[Int]("offset"))
    .contextIn[AuthInfo]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(jsonBody[RecipesResponse])
    .serverLogicSuccess[IO] { case (query, diets, maxCalories, maxCarbs, maxProteins, maxFats, limit, offset, _, _) =>
      val macrosFilter = MacrosFilter(
        maxCalories = maxCalories,
        maxCarbs = maxCarbs,
        maxProteins = maxProteins,
        maxFats = maxFats
      )
      val dietsFilter = diets.map(str => DietaryRequirement.valueOf(str.capitalize))

      recipeService
        .search(
          query = query,
          macrosFilter = macrosFilter,
          dietsFilter = dietsFilter,
          limit = limit,
          offset = offset
        )
        .map(RecipesResponse(_))
    }

  private val recipeRecommendations = endpoint.get
    .in(recipeRecommendationsUrl)
    .contextIn[AuthInfo]()
    .errorOut(statusCode(StatusCode.Unauthorized))
    .out(jsonBody[RecipesResponse])
    .serverLogicSuccess[IO] { (userId, _) =>
      recipeService
        .recommendations(userId)
        .map(RecipesResponse(_))
    }

  val endpoints = List(recipesSearch, recipeRecommendations)

  val routes =
    val authRoutes = Http4sServerInterpreter[IO]().toContextRoutes(endpoints)
    Ior.right(authRoutes)

end RecipeEndpoints
